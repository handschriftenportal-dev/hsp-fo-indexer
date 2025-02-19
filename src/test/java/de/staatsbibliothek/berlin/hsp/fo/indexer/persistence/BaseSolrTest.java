package de.staatsbibliothek.berlin.hsp.fo.indexer.persistence;

import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.extension.EmbeddedSolr;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

public class BaseSolrTest {

  protected static final EmbeddedSolr embeddedSolr = new EmbeddedSolr(new ClassPathResource("solr"));

  @BeforeEach
  public void setUp() throws Exception {
    embeddedSolr.prepare();
  }

  @AfterEach
  public final void tearDown() {
    embeddedSolr.cleanUp();
  }

  @Bean
  public EmbeddedSolr embeddedSolrExtension() {
    return embeddedSolr;
  }
}
