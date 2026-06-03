package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.util.DateTypeHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.XMLSource;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ContentResolverException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.IContentResolver;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ResultMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class YearPostProcessor implements IAttributePostProcessor {
  private static final Pattern yearPattern = Pattern.compile("^-?\\d{1,4}$");

  /* the date formats that should be considered for parsing the date */
  private static final List<SimpleDateFormat> dateFormats = new ArrayList<>();
  static {
    dateFormats.add(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH));
    dateFormats.add(new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH));
    dateFormats.add(new SimpleDateFormat("yyyy", Locale.ENGLISH));
  }

  /**
   * Parses the year of {@code values} first element
   *
   * @param source       the attribute's underlying {@code XMLSource} annotation
   * @param values       the attributes mapped value
   * @param resultMapper provides a result mapping function
   * @param resolver     resolves external content
   * @return the parsed year, or {@code null} if {@code values} is empty or the first element does not contain a valid {@link Date} representation
   */
  @Override
  public List<String> process(XMLSource source, List<String> values, final ResultMapper resultMapper, IContentResolver... resolver) throws ContentResolverException {
    if (CollectionUtils.isNotEmpty(values) && StringUtils.isNotBlank(values.get(0))) {
      final String value = values.get(0);
      /* check if the given value already contains a valid year */
      if (yearPattern.matcher(value).matches()) {
        return List.of(value);
      } else {
        Optional<Date> date = DateTypeHelper.parseDate(value, dateFormats);
        if (date.isPresent()) {
          String year = DateTypeHelper.getYear(date.get()).toString();
          return List.of(year);
        }
      }
    }
    return Collections.emptyList();
  }
}
