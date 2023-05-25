package de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.extension;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.core.io.ClassPathResource;

public class DefaultSolrSetupExtension implements BeforeEachCallback, AfterEachCallback {

  public EmbeddedSolr solrClient;

  public DefaultSolrSetupExtension() {
    solrClient = new EmbeddedSolr(new ClassPathResource("sol"));
  }

  @Override
  public void beforeEach(final ExtensionContext context) throws Exception {
    solrClient.prepare();
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    solrClient.cleanUp();
  }
}
