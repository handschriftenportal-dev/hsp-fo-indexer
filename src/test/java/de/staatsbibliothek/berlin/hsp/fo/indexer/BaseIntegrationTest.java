package de.staatsbibliothek.berlin.hsp.fo.indexer;

import de.staatsbibliothek.berlin.hsp.fo.indexer.api.IndexerHealthIndicator;
import org.junit.jupiter.api.BeforeAll;

public class BaseIntegrationTest {
  @BeforeAll
  public static void beforeAll() {
    IndexerHealthIndicator.setCriticalException(null);
  }
}