package de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.fixture;

import java.io.IOException;

import static de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.FileResourcesUtils.getFileFromResourceAsStream;

public class FileFixture {
  public static byte[] dataFromResourceFilename(final String filename) throws IOException {
    return getFileFromResourceAsStream(filename).readAllBytes();
  }
}
