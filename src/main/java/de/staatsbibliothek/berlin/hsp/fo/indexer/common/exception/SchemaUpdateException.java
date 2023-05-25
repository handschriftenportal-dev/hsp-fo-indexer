package de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception;

import de.staatsbibliothek.berlin.hsp.fo.indexer.service.SchemaService;

/**
 * Exception wrapper for exceptions that can be thrown by any implementation of {@link SchemaService}
 * These will usually be exceptions of the underlying discovery system
 */

public class SchemaUpdateException extends HSPException {

  /**
   * id for serializing the object
   */
  private static final long serialVersionUID = 1L;

  public SchemaUpdateException(final String message) {
    super(message);
  }

  public SchemaUpdateException(final String message, final boolean isCausedByDependence, final boolean isSerious) {
    super(message, isCausedByDependence, isSerious);
  }

  public SchemaUpdateException(final String message, final Exception ex) {
    super(message, ex);
  }

  public SchemaUpdateException(final String message, final Exception ex, final boolean isCausedByDependence, final boolean isSerious) {
    super(message, ex, isCausedByDependence, isSerious);
  }
}
