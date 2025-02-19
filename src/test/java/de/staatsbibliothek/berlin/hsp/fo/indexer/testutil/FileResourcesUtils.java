package de.staatsbibliothek.berlin.hsp.fo.indexer.testutil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileResourcesUtils {

  public static InputStream getFileFromResourceAsStream(final String fileName) throws IOException {
    Path path = Path.of("", "src/test/resources");
    return new ByteArrayInputStream(Files.readString(path.resolve(fileName))
        .getBytes(StandardCharsets.UTF_8));
  }
}
