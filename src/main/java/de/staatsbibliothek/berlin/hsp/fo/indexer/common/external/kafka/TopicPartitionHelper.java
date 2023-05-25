package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.kafka;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides functions to filter and manipulate {@link TopicPartition} indexed {@link Map}s
 */
public class TopicPartitionHelper {

  private TopicPartitionHelper() {
  }

  /**
   * filter a {@link TopicPartition} indexed {@link Map} by its partition index and topic's name
   *
   * @param topicPartitions
   * @param partition
   * @param topicName
   * @return the matching {@link TopicPartition}, null otherwise
   */
  public static TopicPartition filter(final Map<TopicPartition, OffsetAndMetadata> topicPartitions, final int partition, final String topicName) {

    final Optional<Entry<TopicPartition, OffsetAndMetadata>> tp = topicPartitions.entrySet()
        .stream()
        .filter(e -> e.getKey()
            .partition() == partition && e.getKey()
            .topic()
            .equals(topicName))
        .findFirst();

    return tp.map(Entry::getKey)
        .orElse(null);
  }

  /**
   * filter a {@link Set} of {@link TopicPartition}s by its topic's name
   *
   * @param parts
   * @param topicName
   * @return a {@link Collection} containing the resulting {@link TopicPartition}s
   */
  public static Collection<TopicPartition> filter(final Set<TopicPartition> parts, final String topicName) {
    return parts.stream()
        .filter(part -> part.topic()
            .equals(topicName))
        .collect(Collectors.toList());
  }

  /**
   * Adds {@link OffsetAndMetadata} to a map using {@link TopicPartition} as it's
   * key. If the {@link TopicPartition} is already part of the map, the value will
   * simply be replaced
   *
   * @param offsets
   * @param topic
   * @param partition
   * @param offset
   */
  public static void setOffset(final Map<TopicPartition, OffsetAndMetadata> offsets, final String topic, int partition, long offset) {
    final TopicPartition tp = filter(offsets, partition, topic);
    final OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(offset);

    if (tp != null) {
      offsets.replace(tp, offsetAndMetadata);
    } else {
      offsets.put(new TopicPartition(topic, partition), offsetAndMetadata);
    }
  }

  /**
   * converts the source {@link Map} to a {@link Map} containing OffsetAndMetadata as its values
   *
   * @param source
   * @return the converted {@link Map}
   */
  public static Map<TopicPartition, OffsetAndMetadata> convert(final Map<TopicPartition, Long> source) {
    return source.entrySet()
        .stream()
        .collect(Collectors.toMap(Entry::getKey, entry -> new OffsetAndMetadata(entry.getValue())));
  }

  /**
   * removes all <code>toRemove</code> items from <code>offsets</code>
   *
   * @param offsets
   * @param toRemove the {@link TopicPartition}s to be removed
   * @return the resulting {@link Map}
   */
  public static Map<TopicPartition, OffsetAndMetadata> filter(final Map<TopicPartition, OffsetAndMetadata> offsets, final Collection<TopicPartition> toRemove) {
    return offsets.entrySet()
        .stream()
        .filter(e -> toRemove.stream()
            .anyMatch(tp -> tp.partition() == e.getKey()
                .partition() && tp.topic()
                .equals(e.getKey()
                    .topic())))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }
}
