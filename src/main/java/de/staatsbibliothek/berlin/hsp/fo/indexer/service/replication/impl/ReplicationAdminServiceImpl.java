package de.staatsbibliothek.berlin.hsp.fo.indexer.service.replication.impl;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.ReplicationException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.replication.ReplicationAdminService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.replication.ReplicationRequest;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.replication.ReplicationResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class ReplicationAdminServiceImpl implements ReplicationAdminService {

  protected final SolrClient clientFollower;
  protected String solrVersion;
  protected String solrCore;

  @Autowired
  public void setSolrCore(@Value("${solr.core}") final String solrCore) {
    this.solrCore = solrCore;
  }

  @Autowired
  public void setSolrVersion(@Value("${solr.version}") final String solrVersion) {
    this.solrVersion = solrVersion;
  }

  public ReplicationAdminServiceImpl(@Qualifier("followerClient") @Autowired(required = false) SolrClient clientFollower) {
    this.clientFollower = clientFollower;
  }

  @Override
  public boolean pollingEnabled() throws ReplicationException {
    if(this.clientFollower == null) {
      return false;
    }
    final ReplicationRequest req = new ReplicationRequest(SolrRequest.METHOD.GET);
    req.fetchDetails();
    try {
      final ReplicationResponse response = req.process(this.clientFollower, solrCore);
      return response.isReplicationEnabled(solrVersion);
    } catch (SolrServerException | IOException e) {
      throw new ReplicationException(e.getMessage(), e, true, true);
    }
  }

  @Override
  public void enablePolling() throws ReplicationException {
    if(this.clientFollower == null) {
      return;
    }

    final ReplicationRequest req = new ReplicationRequest(SolrRequest.METHOD.GET);
    req.enablePolling();
    try {
      final ReplicationResponse response = req.process(this.clientFollower, solrCore);
      if(response.getStatus() == 200) {
        log.info("polling enabled for solr follower");
      } else {
        throw new ReplicationException("Unrecognized status code while enabling polling on solr follower", response.getException(), true, true);
      }
    } catch (SolrServerException | IOException e) {
      throw new ReplicationException(e.getMessage(), e, true, true);
    }
  }

  @Override
  public void disablePolling() throws ReplicationException {
    if(this.clientFollower == null) {
      return;
    }

    final ReplicationRequest req = new ReplicationRequest(SolrRequest.METHOD.GET);
    req.disablePoll();
    try {
      final ReplicationResponse response = req.process(this.clientFollower, solrCore);
      if(response.getStatus() == 200) {
        log.info("polling disabled for solr follower");
      } else {
        throw new ReplicationException("Unrecognized status code {} while disabling polling on solr follower", true, true);
      }
    } catch (SolrServerException | IOException e) {
      throw new ReplicationException(e.getMessage(), e, true, true);
    }
  }
}
