package de.staatsbibliothek.berlin.hsp.fo.indexer.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Helper for date conversion
 */
public class DateTypeHelper {

  private static final Logger logger = LoggerFactory.getLogger(DateTypeHelper.class);

  private DateTypeHelper() {
  }

  /**
   * @param dateString  the string to parse the date out of
   * @param dateFormats the patterns to use
   * @return an {@link Optional<Date>} containing the parsed date if one of the patterns matched, an empty {@code Optional} otherwise
   */
  public static Optional<Date> parseDate(final String dateString, final List<SimpleDateFormat> dateFormats) {
    for (SimpleDateFormat df : dateFormats) {
      try {
        Date d = df.parse(dateString);
        return Optional.of(d);
      } catch (ParseException e) {
        // continue with next format
      }
    }
    logger.warn("was not able to parse date of {}", dateString);
    return Optional.empty();
  }
}