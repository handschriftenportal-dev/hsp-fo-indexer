package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.HSPException;

/**
 * Exception wrapper for exceptions that can be thrown by any implementation of {@link IContentResolver}
 */
public class ContentResolverException extends HSPException {

  /**
   * id for serializing the object
   */
  private static final long serialVersionUID = 1L;

  public ContentResolverException(String message) {
    super(message);
  }

  public ContentResolverException(final String message, final boolean isCausedByDependence, final boolean isSerious) {
    super(message, isCausedByDependence, isSerious);
  }

  public ContentResolverException(String message, Exception ex) {
    super(message, ex);
  }

  public ContentResolverException(String message, Exception ex, boolean isCausedByDependence, final boolean isSerious) {
    super(message, ex, isCausedByDependence, isSerious);
  }
}
