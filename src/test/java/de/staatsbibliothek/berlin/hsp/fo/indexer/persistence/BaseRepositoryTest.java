package de.staatsbibliothek.berlin.hsp.fo.indexer.persistence;

import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.SolrRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;

class BaseRepositoryTest extends BaseSolrTest {

  protected SolrRepositoryImpl<HspBaseTestClass> persistenceService;

  @BeforeEach
  final void init() throws Exception {
    super.setUp();
    persistenceService = new SolrRepositoryImpl<>(HspBaseTestClass.class);
    persistenceService.setSolrClient(BaseSolrTest.embeddedSolr.getSolrServer()
        .getSolrClient("test"));
    persistenceService.setCollectionName("test");
  }
}
