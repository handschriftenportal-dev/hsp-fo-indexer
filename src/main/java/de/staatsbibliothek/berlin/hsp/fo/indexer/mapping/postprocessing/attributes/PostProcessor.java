package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.GNDEntity;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.GNDEntityType;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.XMLSource;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ContentResolverException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.IContentResolver;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.PostProcessingHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ResultMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public abstract class PostProcessor {
  // pattern for matching authority file ids
  private static final Pattern AUTHORITY_FILE_PATTERN = Pattern.compile("^NORM-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
  /**
   * Processes the given attributes data whether by using one or many of the given {@code IContentResolvers} or not
   *
   * @param source       the attribute's underlying {@code XMLSource} annotation
   * @param values       the attributes mapped value
   * @param resultMapper provides a result mapping function
   * @param resolver     resolves external content
   * @return the processed values
   */
  protected List<String> process(final XMLSource source, final List<String> values, final ResultMapper resultMapper, final GNDEntityType type, final IContentResolver... resolver) throws ContentResolverException {
    List<GNDEntity> entities = Collections.emptyList();
    if (CollectionUtils.isNotEmpty(values) && StringUtils.isNotBlank(values.get(0)) && AUTHORITY_FILE_PATTERN.matcher(values.get(0)).matches()) {
      entities = PostProcessingHelper.getAuthorityFiles(values.get(0), type, resolver);

      if (entities.size() > 1 && log.isInfoEnabled()) {
        log.info("Multiple authority files for {} were found. Only the first will be used", values.get(0));
      }
      if(entities.isEmpty() && log.isInfoEnabled()) {
        log.warn("No authority file found for {}", values.get(0));
      }
    }
    return resultMapper.getMapper().apply(entities);
  }
}
