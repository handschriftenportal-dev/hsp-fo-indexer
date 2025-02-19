package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.GNDEntityType;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.XMLSource;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ContentResolverException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.IContentResolver;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ResultMapper;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * The {@code process} implementation of this class fetches a corporate authority file by using the given {@code authority file id}
 * and returns all of its values as {@code List} if an authority file was found. If the given {@code authority file id} is blank no request is performed.
 * In this case, an empty list is returned.
 */
public class PersonPostProcessor extends PostProcessor implements IAttributePostProcessor {

  /**
   * Processes the given attributes data whether by using one or many of the given {@code IContentResolvers} or not
   *
   * @param source       the attribute's underlying {@code XMLSource} annotation
   * @param values       the attributes mapped value (representing the ids)
   * @param resultMapper provides a result mapping function
   * @param resolver     resolves external content
   * @return the processed values
   */
  @Override
  public List<String> process(final XMLSource source, final List<String> values, final ResultMapper resultMapper, final IContentResolver... resolver) throws ContentResolverException {
    final List<String> processedResult = process(source, values, resultMapper, GNDEntityType.PERSON, resolver);

    if(CollectionUtils.isEmpty(processedResult)) {
      return values;
    }
    return processedResult;
  }
}
