package de.staatsbibliothek.berlin.hsp.fo.indexer.persistence;

import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.PersistenceServiceImpl;
import org.junit.jupiter.api.BeforeEach;

public class BasePersistenceServiceTest extends BaseSolrTest {

  protected PersistenceServiceImpl<HspBaseTestClass> persistenceService;

  @BeforeEach
  public final void init() throws Exception {
    super.setUp();
    persistenceService = new PersistenceServiceImpl<>(HspBaseTestClass.class);
    persistenceService.setSolrClient(BaseSolrTest.embeddedSolr.getSolrServer()
        .getSolrClient("test"));
    persistenceService.setCollectionName("test");
  }
}
