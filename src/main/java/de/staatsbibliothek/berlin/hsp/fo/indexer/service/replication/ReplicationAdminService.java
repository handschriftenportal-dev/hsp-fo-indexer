package de.staatsbibliothek.berlin.hsp.fo.indexer.service.replication;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.ReplicationException;

public interface ReplicationAdminService {
  void enablePolling() throws ReplicationException;
  void disablePolling() throws ReplicationException;
  boolean pollingEnabled() throws ReplicationException;
}
