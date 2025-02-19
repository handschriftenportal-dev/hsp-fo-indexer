package de.staatsbibliothek.berlin.hsp.fo.indexer.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Helper for reading resources
 */
public class FileHelper {

  private FileHelper() {
  }

  private static <T> T fromJSON(final TypeReference<T> type, final String jsonPacket) throws JsonProcessingException {
    return new ObjectMapper().readValue(jsonPacket, type);
  }

  /**
   * Reads a resource file from the given {@code resourceLocation}
   *
   * @param tRef             the object type that the resouce's content should be mapped to
   * @param resourceLocation the resource's location
   * @param <T>              the type the resource's content should be mapped to
   * @return the read object
   * @throws IOException in case there is any problem with reading the resource
   */
  public static <T> T fromLocation(final TypeReference<T> tRef, final String resourceLocation) throws IOException {
    final URL resource = Resources.getResource(resourceLocation);
    final String fileContent = Resources.toString(resource, StandardCharsets.UTF_8);
    return fromJSON(tRef, fileContent);
  }
}
