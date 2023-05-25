package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing;

/**
 *
 */
public interface IContentResolver {
  Object resolve(final String uri, final ContentInformation contentInformation) throws ContentResolverException;
}
