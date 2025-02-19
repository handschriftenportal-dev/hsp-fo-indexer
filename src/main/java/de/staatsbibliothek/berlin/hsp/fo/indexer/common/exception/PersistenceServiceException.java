package de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception;

import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.PersistenceService;

/**
 * Exception wrapper for exceptions that can be thrown by any implementation of {@link PersistenceService}
 */
public class PersistenceServiceException extends HSPException {

  /**
   * id for serializing the object
   */
  private static final long serialVersionUID = -1799594213035320669L;

  public PersistenceServiceException(final String message) {
    super(message);
  }

  public PersistenceServiceException(final String message, final boolean isCausedByDependant, final boolean isCritical) {
    super(message, isCausedByDependant, isCritical);
  }

  public PersistenceServiceException(final String message, final Exception cause) {
    super(message, cause);
  }

  public PersistenceServiceException(final String message, final Exception cause, final boolean isCausedByDependant, final boolean isCritical) {
    super(message, cause, isCausedByDependant, isCritical);
  }
}
