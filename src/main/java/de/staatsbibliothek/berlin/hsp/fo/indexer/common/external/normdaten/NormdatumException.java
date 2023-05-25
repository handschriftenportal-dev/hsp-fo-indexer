package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ContentResolverException;

/**
 * Exception wrapper for exceptions that can be thrown by the {@link NormdatenService}
 */
public class NormdatumException extends ContentResolverException {

  /**
   * id for serializing the object
   */
  private static final long serialVersionUID = 1L;

  public NormdatumException(final String message) {
    super(message);
  }

  public NormdatumException(final String message, final boolean isCausedByDependence, final boolean isSerious) {
    super(message, isCausedByDependence, isSerious);
  }

  public NormdatumException(final String message, final Exception ex) {
    super(message, ex);
  }

  public NormdatumException(final String message, final Exception ex, final boolean isCausedByDependence, final boolean isSerious) {
    super(message, ex, isCausedByDependence, isSerious);
  }
}
