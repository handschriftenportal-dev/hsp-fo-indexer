package de.staatsbibliothek.berlin.hsp.fo.indexer.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.collections4.IteratorUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

class JsonHelperTest {

  static ObjectMapper mapper;

  @BeforeAll
  static void init() {
    mapper = new ObjectMapper();
  }

  private ArrayNode getArrayNode(String... values) {
    final ArrayNode result = mapper.createArrayNode();
    for(String val : values) {
      result.add(val);
    }
    return result;
  }

  private List<String> getValues(ArrayNode array) {
    return IteratorUtils.toList(array.elements()).stream()
            .map(JsonNode::textValue)
            .collect(Collectors.toList());
  }

  @Test
  void whenRemoveDuplicatesIsCalled_thenDuplicatesAreRemoved() {
    final ArrayNode values = getArrayNode("a", "b", "a", "c", "b", "ab", "d");

    JsonHelper.removeDuplicates(values);

    assertThat(getValues(values), contains("a", "b", "c", "ab", "d"));
  }

  @Test
  void whenIsArrayIsCalledAndAllNodesAreArraysOrNull_thenTrueIsReturned() {
    final ObjectNode node01 = mapper.createObjectNode();
    node01.putArray("test");
    final ObjectNode node02 = mapper.createObjectNode();
    node02.putNull("test");
    List<JsonNode> nodes = List.of(node01, node02);

    final boolean isArray = JsonHelper.isArray("test", nodes);

    assertThat(isArray, is(true));
  }

  @Test
  void whenIsArrayIsCalledAndNotAllNodesAreArraysOrNull_thenFalseIsReturned() {
    final ObjectNode node01 = mapper.createObjectNode();
    node01.putArray("test");
    final ObjectNode node02 = mapper.createObjectNode();
    node02.put("test", 4);
    List<JsonNode> nodes = List.of(node01, node02);

    final boolean isArray = JsonHelper.isArray("test", nodes);

    assertThat(isArray, is(false));
  }
}
