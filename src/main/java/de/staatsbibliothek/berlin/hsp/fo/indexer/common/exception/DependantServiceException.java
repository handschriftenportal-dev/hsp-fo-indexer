package de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception;

public class DependantServiceException extends HSPException {
  public DependantServiceException(String message, Exception e, boolean isCausedByDependence, boolean isCritical) {
    super(message, e, isCausedByDependence, isCritical);
  }
}
