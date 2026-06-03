package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.impl.AuthorityFileServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ContentResolverException;

import java.io.Serial;

/**
 * Exception wrapper for exceptions that can be thrown by the {@link AuthorityFileServiceImpl}
 */
public class AuthorityFileServiceException extends ContentResolverException {

  /**
   * id for serializing the object
   */
  @Serial
  private static final long serialVersionUID = 1L;

  public AuthorityFileServiceException(final String message, final boolean isCausedByDependence, final boolean isSerious) {
    super(message, isCausedByDependence, isSerious);
  }
}
