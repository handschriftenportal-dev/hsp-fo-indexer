package de.staatsbibliothek.berlin.hsp.fo.indexer.common.util;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Helper for date conversion
 */
@Slf4j
public class DateTypeHelper {

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
    log.warn("was not able to parse date of {}", dateString);
    return Optional.empty();
  }

  /**
   * Extracts the year from a date
   * @param date
   * @return the year
   */
  public static Integer getYear(@Nonnull final Date date) {
      final Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      return cal.get(Calendar.YEAR);
  }
}