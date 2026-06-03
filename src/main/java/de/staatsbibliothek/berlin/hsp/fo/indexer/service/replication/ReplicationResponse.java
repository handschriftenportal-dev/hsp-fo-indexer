package de.staatsbibliothek.berlin.hsp.fo.indexer.service.replication;

import com.jayway.jsonpath.JsonPath;
import org.apache.solr.client.solrj.response.SolrResponseBase;

public class ReplicationResponse extends SolrResponseBase {
  public boolean isReplicationEnabled(final String solrVersion) {
    final String isReplicationDisabled = JsonPath.read(this.getResponse().jsonStr(), getReplicationInfoPath(solrVersion));
    return !Boolean.parseBoolean(isReplicationDisabled);
  }

  @Override
  public int getStatus() {
    return "OK".equals(this.getResponse().get("status")) ? 200 : 0;
  }

  private String getReplicationInfoPath(final String solrVersion) {
    final String name = solrVersion.startsWith("9") ? "follower" : "slave";
    return String.format("$.details.%s.isPollingDisabled", name);
  }
}
