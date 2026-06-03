package de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception;

public class ReplicationException extends HSPException {

  public ReplicationException(String message, Exception ex, boolean isCausedByDependence, boolean isCritical) {
    super(message, ex, isCausedByDependence, isCritical);
  }

  public ReplicationException(String message, boolean isCausedByDependence, boolean isCritical) {
    super(message, isCausedByDependence, isCritical);
  }
}
