package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.kafka;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.kafka.TopicPartitionHelper;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;

class TopicPartitionHelperTest {

  final TopicPartition firstTp = new TopicPartition("test", 0);
  final TopicPartition secondTp = new TopicPartition("test1", 2);
  private Map<TopicPartition, OffsetAndMetadata> topicPartitions;

  @BeforeEach
  public void setUp() {
    topicPartitions = new HashMap<>();
    topicPartitions.put(firstTp, new OffsetAndMetadata(12));
    topicPartitions.put(secondTp, new OffsetAndMetadata(3));
  }

  @Test
  void filterByPartitionAndTopic_ShouldReturnTestTopicPartitions_WhenTopicNameIsTest() {
    final TopicPartition tp = TopicPartitionHelper.filter(topicPartitions, 0, "test");
    assertThat(tp, is(equalTo(firstTp)));
  }

  @Test
  void filterByPartitionAndTopic_ShouldReturnNull_WhenTopicNameIsNotFound() {
    final TopicPartition tp = TopicPartitionHelper.filter(topicPartitions, 0, "test_wrong");
    assertThat(tp, is(nullValue()));
  }

  @Test
  void setOffset_ShouldReplaceOffset_WhenTopicPartitionIsAlreadyThere() {
    TopicPartitionHelper.setOffset(topicPartitions, "test", 0, 99);
    assertThat(topicPartitions, is(aMapWithSize(2)));
    assertThat(topicPartitions.get(firstTp)
        .offset(), is(99L));
  }

  @Test
  void setOffset_ShouldAddOffset_WhenTopicPartitionIsNotAlreadyThere() {
    TopicPartitionHelper.setOffset(topicPartitions, "test", 1, 99);

    assertThat(topicPartitions, is(aMapWithSize(3)));
    assertThat(TopicPartitionHelper.filter(topicPartitions, 1, "test"), is(notNullValue()));
    assertThat(topicPartitions.get(TopicPartitionHelper.filter(topicPartitions, 1, "test"))
        .offset(), is(99L));
  }

  @Test
  void filterByTopic_ShouldReturnTestTopicPartition_WhenTopicNameIsTest() {
    final Collection<TopicPartition> filteredTopicPartitions = TopicPartitionHelper.filter(topicPartitions.keySet(), "test");

    assertThat(filteredTopicPartitions, hasSize(1));
    assertThat(filteredTopicPartitions, hasItem(firstTp));
  }

  @Test
  void filterByTopic_ShouldReturnEmptyCollection_WhenTopicNameIsNotFound() {
    final Collection<TopicPartition> filteredTopicPartitions = TopicPartitionHelper.filter(topicPartitions.keySet(), "test2");
    assertThat(filteredTopicPartitions, hasSize(0));
  }

  @Test
  void filterByTopicPartitions_ShouldReturnFirstTp_WhenPartitionTopicEqualsFirstTp() {
    final Map<TopicPartition, OffsetAndMetadata> filteredTopicPartitions = TopicPartitionHelper.filter(topicPartitions, new HashSet<>(List.of(firstTp)));
    assertThat(filteredTopicPartitions, is(aMapWithSize(1)));
    assertThat(filteredTopicPartitions, hasKey(firstTp));
  }
}
