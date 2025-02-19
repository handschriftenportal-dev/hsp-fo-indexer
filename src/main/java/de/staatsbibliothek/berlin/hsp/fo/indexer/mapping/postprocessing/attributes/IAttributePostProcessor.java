package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.XMLSource;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ContentResolverException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.IContentResolver;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ResultMapper;

import java.util.List;

/**
 *
 */
public interface IAttributePostProcessor {
  /**
   * Processes the given attributes data whether by using one or many of the given {@code IContentResolvers}
   *
   * @param source       the attribute's underlying {@code XMLSource} annotation
   * @param values       the attributes mapped value
   * @param resultMapper provides a result mapping function
   * @param resolver     resolves external content
   * @return the processed values
   */
  List<String> process(final XMLSource source, final List<String> values, final ResultMapper resultMapper, final IContentResolver... resolver) throws ContentResolverException;
}
