package de.staatsbibliothek.berlin.hsp.fo.indexer.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IteratorUtils;

import java.util.*;

@Slf4j
public class JsonHelper {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private JsonHelper() {
  }

  /**
   * creates an instance of type {@code T} based on a JsonNode
   * @param jsonNode the JsonNode representation
   * @param clazz target class
   * @return the instance
   */
  public static <T> Optional<T> fromJsonTree(final JsonNode jsonNode, final Class<T> clazz) {
    try {
      return Optional.of(objectMapper.treeToValue(jsonNode, clazz));
    } catch(JsonProcessingException e) {
      log.error("error while converting jsonTrr to object", e);
    }
    return Optional.empty();
  }

  /**
   * creates a list of class instances of type {@code T} based on several JsonNodes
   * @param jsonNodes the JsonNode representations
   * @param clazz target class
   * @return {@code List} containing the instances
   */
  public static <T> List<T> fromJsonTree(final List<JsonNode> jsonNodes, final Class<T> clazz) {
    final List<T> result = new ArrayList<>(jsonNodes.size());
    for(JsonNode node : jsonNodes) {
      try {
        result.add(objectMapper.treeToValue(node, clazz));
      } catch(JsonProcessingException e) {
        log.error("error while deserializing json to object", e);
      }
    }
    return result;
  }

  /**
   * converts an object to its JSON representation
   * @param object the object to be converted
   * @return The JSON string wrapped in an {@link Optional}
   */
  public static <T> Optional<String> toJson(final T object) {
    try {
      return Optional.of(objectMapper.writeValueAsString(object));
    } catch (JsonProcessingException e) {
      log.warn("Error while serializing object to json", e);
    }
    return Optional.empty();
  }

  /**
   * converts an object to its JSON tree representation
   * @param object the object to be converted
   * @return A {@link JsonNode} wrapped in an {@link Optional}
   */
  public static <T> Optional<JsonNode> toJsonTree(final T object) {
    final Optional<String> jsonString = toJson(object);
    if(jsonString.isPresent()) {
      try {
        return Optional.of(objectMapper.readTree(jsonString.get()));

      } catch (JsonProcessingException e) {
        log.warn("Error while reading json tree of object", e);
      }
    }
    return Optional.empty();
  }

  /**
   * Creates an {@link ArrayNode} instance
   * @return the {@link ArrayNode}
   */
  public static ArrayNode createJsonArray() {
    return objectMapper.createArrayNode();
  }

  /**
   * removes duplicates from an {@code ArrayNode} by comparing the nodes by using the {{@link JsonNode#toString()}} method
   * @param arrayNode the {@code ArrayNode} that to be cleansed of duplicates
   */
  public static void removeDuplicates(final ArrayNode arrayNode) {
    final Set<String> checked = new HashSet<>();
    final Iterator<JsonNode> it = arrayNode.elements();
    while(it.hasNext()) {
      final JsonNode currentNode = it.next();
      if(checked.contains(currentNode.toString())) {
        it.remove();
      } else {
        checked.add(currentNode.toString());
      }
    }
  }

  /**
   * checks if all node's property of the given name is an array. NullNodes will be ignored. If allem
   * @param propertyName the property to check
   * @param nodes the nodes to check
   * @return @code{TRUE} if the property is an array, {FALSE} otherwise
   */
  public static boolean isArray(final String propertyName, final List<JsonNode> nodes) {
    boolean isArray = false;
    for(JsonNode node : nodes) {
      if(node.get(propertyName) instanceof ArrayNode) {
        isArray = true;
      } else if(!(node.get(propertyName) instanceof NullNode)) {
        return false;
      }
    }
    return isArray;
  }

  /**
   * Collects a list of all field names that appear in all given {@link JsonNode}s
   * @param nodes the JsonNodes that should be considered for collecting field names
   * @return a distinct {@link List} of field names
   */
  public static List<String> getFieldNames(final List<JsonNode> nodes) {
    List<String> result = new LinkedList<>();
    for(int i = 0; i < nodes.size(); i++) {
      List<String> values = IteratorUtils.toList(nodes.get(i).fieldNames());
      if (i == 0) {
        result.addAll(values);
      } else {
        result.retainAll(values);
      }
    }
    return result;
  }
}
