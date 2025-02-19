package de.staatsbibliothek.berlin.hsp.fo.indexer.service.replication;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;

public class ReplicationRequest extends SolrRequest<ReplicationResponse> {

  private static final String COMMAND = "command";
  private static final String COMMAND_DETAILS = "details";
  private static final String COMMAND_DISABLE_POLL = "disablepoll";
  private static final String PATH = "/replication";

  private final ModifiableSolrParams params;

  public ReplicationRequest(METHOD m) {
    super(m, PATH);
    this.params = new ModifiableSolrParams();
  }

  /**
   * @return
   */
  @Override
  public String getRequestType() {
    return SolrRequestType.ADMIN.toString();
  }

  @Override
  public SolrParams getParams() {
    return params;
  }

  @Override
  protected ReplicationResponse createResponse(SolrClient client) {
    return new ReplicationResponse();
  }

  public void enablePolling() {
    this.params.set(COMMAND, COMMAND_DISABLE_POLL);
  }

  public void disablePoll() {
    this.params.set(COMMAND, COMMAND_DISABLE_POLL);
  }

  public void fetchDetails() {
    this.params.set(COMMAND, COMMAND_DETAILS);
  }
}
