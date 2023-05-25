package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten.GNDEntity;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten.GNDEntityType;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.XMLSource;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ContentResolverException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.IContentResolver;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.PostProcessingHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ResultMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * The {@code process} implementation of this class fetches a corporate normdatum by using the given {@code normdatumURI}
 * and returns all of its values as {@code List} if a normdatum was found. If the given {@code normdatumURI} is blank no request is performed.
 * In this case, an empty list is returned.
 */
public class PlacePostProcessor implements IAttributePostProcessor {

  static final Logger logger = LoggerFactory.getLogger(PlacePostProcessor.class);

  /**
   * Processes the given attributes data whether by using one or many of the given {@code IContentResolvers} or not
   *
   * @param source       the attribute's underlying {@code XMLSource} annotation
   * @param values       the attributes mapped value
   * @param resultMapper provides a result mapping function
   * @param resolver     resolves external content
   * @return the processed values
   */
  @Override
  public List<String> process(XMLSource source, List<String> values, final ResultMapper resultMapper, IContentResolver... resolver) throws ContentResolverException {
    List<GNDEntity> entities = Collections.emptyList();
    if (CollectionUtils.isNotEmpty(values) && StringUtils.isNotBlank(values.get(0))) {
      entities = PostProcessingHelper.getNormdatumList(values.get(0), GNDEntityType.PLACE, resolver);
      if (entities.size() > 1) {
        logger.info("Multiple Normdatum entries for {} were found. Only the first will be used", values.get(0));
      }
      if(entities.isEmpty()) {
        logger.warn("No Normdatum found for {}", values.get(0));
      }
    }
    return resultMapper.getMapper()
        .apply(entities);
  }
}