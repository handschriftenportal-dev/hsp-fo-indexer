package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.kafka;

import de.staatsbibliothek.berlin.hsp.fo.indexer.api.IndexerHealthIndicator;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ContentResolverException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.model.ActivityStreamMessage;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.RebalanceInProgressException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

/**
 * Receives kafka messages including manual offset commit in case of an fatal error
 * that keeps us from any further message processing.
 */
@Component
public class KafkaMessageReceiver implements ConsumerSeekAware, ConsumerRebalanceListener {

  private static final Logger logger = LoggerFactory.getLogger(KafkaMessageReceiver.class);
  private final KafkaConsumer<String, ActivityStreamMessage> consumer;
  private final Map<TopicPartition, OffsetAndMetadata> partitionOffsets = new HashMap<>();
  private final List<Integer> revokedPartitions = new ArrayList<>();
  private String topicName;
  private boolean resetOffset;
  private IKafkaMessageHandler kafkaMessageHandler;
  private ConsumerRecord<String, ActivityStreamMessage> lastRecord;

  public KafkaMessageReceiver(
      @Value("${kafka.bootstrap-servers}") final String bootstrapAddress,
      @Value("${kafka.groupid}") final String groupId) {
    consumer = new KafkaConsumer<>(consumerConfigs(bootstrapAddress, groupId), new StringDeserializer(), new JsonDeserializer<>(ActivityStreamMessage.class));
  }

  @Autowired
  public void setKafkaMessageHandler(IKafkaMessageHandler messageHandler) {
    this.kafkaMessageHandler = messageHandler;
  }

  @Autowired
  public void setTopicName(@Value("${kafka.topic}") final String topicName) {
    this.topicName = topicName;
  }

  @Autowired
  public void setResetOffset(
      @Value("${kafka.resetOffset:false}") final boolean resetOffset) {
    this.resetOffset = resetOffset;
  }

  public Map<String, Object> consumerConfigs(final String bootstrapAddress, final String groupId) {
    Map<String, Object> props = new HashMap<>();

    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
    props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 1000000);
    props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);

    return props;
  }

  @Scheduled(fixedRateString = "${common.restart-interval}")
  public void restart() {
    if (!IndexerHealthIndicator.isHealthy() && IndexerHealthIndicator.getUnhealthyException() != null && IndexerHealthIndicator.getUnhealthyException()
        .isCausedByDependence()) {
      IndexerHealthIndicator.resetException();
      start();
    }
  }

  /**
   * start receiving kafka messages and handle them by using the given {@see IKafkaMessageHandler}
   */
  public void start() {
    consumer.subscribe(Collections.singletonList(topicName));

    while (IndexerHealthIndicator.isHealthy()) {
      if (resetOffset) {
        resetOffset();
      }

      ConsumerRecords<String, ActivityStreamMessage> records = consumer.poll(Duration.ofMillis(100));
      for (ConsumerRecord<String, ActivityStreamMessage> consumerRecord : records) {
        if (!IndexerHealthIndicator.isHealthy()) {
          stop();
          return;
        }

        /* if record belongs to previously revoked partition, skip processing */
        if (revokedPartitions.contains(consumerRecord.partition())) {
          logger.info("The record's partition was revoked, will skip processing.");
          continue;
        }

        if (logger.isInfoEnabled()) {
          logger.info("Receiving Record at offset {} with topic {} partition {} key {}", consumerRecord.offset(), consumerRecord.topic(), consumerRecord.partition(), consumerRecord.key());
        }
        try {
          kafkaMessageHandler.handleMessage(consumerRecord.value());
          /* if partition switched, save offset for the old partition */
          if (lastRecord != null && lastRecord.partition() != consumerRecord.partition()) {
            TopicPartitionHelper.setOffset(partitionOffsets, topicName, lastRecord.partition(), lastRecord.offset() + 1);
          }
          lastRecord = consumerRecord;
        } catch (PersistenceServiceException | ContentResolverException e) {
          if (e.isSerious()) {
            IndexerHealthIndicator.setUnhealthyException(e);
          }
        }
      }
      try {
        if (lastRecord != null) {
          TopicPartitionHelper.setOffset(partitionOffsets, topicName, lastRecord.partition(), lastRecord.offset() + 1);
          consumer.commitSync(partitionOffsets);
          /* avoid committing while awaiting new messages */
          lastRecord = null;
          partitionOffsets.clear();
          revokedPartitions.clear();
        }
      } catch (CommitFailedException | RebalanceInProgressException e) {
        logger.error("Commit failed {}: {}", e.getMessage(), e);
      }
    }
    stop();
  }

  /**
   * stop receiving kafka messages and commit set the offset of the last handled message
   */
  public void stop() {
    try {
      consumer.unsubscribe();
      if (lastRecord != null) {
        TopicPartitionHelper.setOffset(partitionOffsets, topicName, lastRecord.partition(), lastRecord.offset() + 1);
      }
      consumer.commitSync(partitionOffsets);
    } catch (Exception ex) {
      logger.error("An error occurred while committing the offset to kafka {}: {}", ex.getMessage(), ex);
    }
  }

  private void resetOffset() {
    Collection<TopicPartition> tp = TopicPartitionHelper.filter(consumer.assignment(), topicName);
    if (!tp.isEmpty()) {
      consumer.seekToBeginning(tp);
      final Map<TopicPartition, Long> beginningOffsets = consumer.beginningOffsets(tp);
      consumer.commitSync(TopicPartitionHelper.convert(beginningOffsets));
      resetOffset = false;
    }
  }

  @Override
  public void onPartitionsRevoked(
      @NotNull Collection<TopicPartition> partitions) {
    /* get revoked partitions and commit offset */
    Map<TopicPartition, OffsetAndMetadata> revokedOffsets = TopicPartitionHelper.filter(partitionOffsets, partitions);
    try {
      consumer.commitSync(revokedOffsets);
    } catch (Exception ex) {
      /* if something went wrong, the affected records will be processed a second time, but we don't care */
      logger.error("Commit failed after partition revoking {}: {}", ex.getMessage(), ex);
    }

    /* remove revoked partitions from offset map */
    for (TopicPartition revokedTp : partitions) {
      final TopicPartition tp = TopicPartitionHelper.filter(partitionOffsets, revokedTp.partition(), revokedTp.topic());
      if (tp != null) {
        partitionOffsets.remove(tp);
        revokedPartitions.add(tp.partition());
      }
    }
  }

  @Override
  public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
    // nothing to do here
  }
}
