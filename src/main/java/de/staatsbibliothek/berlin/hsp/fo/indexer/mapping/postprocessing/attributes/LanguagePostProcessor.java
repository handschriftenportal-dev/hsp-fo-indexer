package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.XMLSource;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ContentResolverException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.IContentResolver;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ResultMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * The {@code process} implementation of this class checks each values is a valid authority file id, otherwise it will be replaced with "unknown"
 */
public class LanguagePostProcessor extends PostProcessor implements IAttributePostProcessor {

  private static final Pattern AUTHORITY_FILE_PATTERN = Pattern.compile("^NORM-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

  /**
   * @param source       the attribute's underlying {@code XMLSource} annotation
   * @param values       the attributes mapped value
   * @param resultMapper provides a result mapping function
   * @param resolver     resolves external content
   * @return the processed values
   * @throws ContentResolverException
   */
  @Override
  public List<String> process(XMLSource source, List<String> values, ResultMapper resultMapper, IContentResolver... resolver) throws ContentResolverException {
    final String fallback = "unknown";
    return CollectionUtils.emptyIfNull(values)
        .stream()
        .map(v -> StringUtils.isEmpty(v) || isValidAuthorityFileId(v) ? v : fallback)
        .distinct()
        .toList();
  }

  /**
   * Determines if the value is a valid authority file id
   *
   * @param value the value to check
   * @return true if the value is a valid authority file id, false otherwise
   */
  private boolean isValidAuthorityFileId(final String value) {
    return AUTHORITY_FILE_PATTERN.matcher(value).matches();
  }
}
