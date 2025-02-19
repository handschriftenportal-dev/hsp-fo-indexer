package de.staatsbibliothek.berlin.hsp.fo.indexer.service;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.ReplicationException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.BaseSolrTest;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.replication.ReplicationAdminService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.replication.impl.ReplicationAdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ReplicationServiceTest extends BaseSolrTest {

  private static ReplicationAdminService replicationService;

  @BeforeEach
  void init() throws ReplicationException {
    replicationService = new ReplicationAdminServiceImpl(BaseSolrTest.embeddedSolr.getSolrServer().getSolrClient("hsp"));
    ((ReplicationAdminServiceImpl) replicationService).setSolrVersion("8");
    replicationService.enablePolling();
  }

  //@Test
  void givenActivatedReplication_whenDisablingReplication_thenReplicationIsNotEnabledAfterwards() throws Exception {
    boolean isEnabled;
    isEnabled = replicationService.pollingEnabled();
    assertThat(isEnabled, is(true));
    replicationService.disablePolling();
    isEnabled = replicationService.pollingEnabled();
    assertThat(isEnabled, is(false));
  }
}