package de.staatsbibliothek.berlin.hsp.fo.indexer.common.util;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.util.DateTypeHelper;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static org.exparity.hamcrest.date.DateMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class DataTypeHelperTest {

  @Test
  void whenParsingDateWithMatchingPattern_thenDateInstanceIsReturned() throws Exception {
    final List<SimpleDateFormat> formats = List.of(new SimpleDateFormat("yyyy"));
    final Optional<Date> parsedDate = DateTypeHelper.parseDate("1999", formats);
    final Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, 1999);

    assertThat(parsedDate, isPresentAnd(sameYear(cal.getTime())));
  }

  @Test()
  void whenParsingDateWithNotMatchingPattern_theEmptyOptionalIsReturned() throws Exception {
    final List<SimpleDateFormat> formats = List.of(new SimpleDateFormat("HH:mm"));

    final Optional<Date> parsed = DateTypeHelper.parseDate("1999", formats);

    assertThat(parsed, isEmpty());
  }

  @Test
  void whenParsingDateWithMultiplePattern_thenCorrectDateInstanceIsReturned() throws Exception {
    final List<SimpleDateFormat> formats = List.of(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy"), new SimpleDateFormat("yyyy-MM-dd"), new SimpleDateFormat("yyyy"));
    Optional<Date> parsedDate = DateTypeHelper.parseDate("1999-05-05", formats);
    assertThat(parsedDate, isPresentAnd(isDay(1999, Month.MAY, 5)));

    parsedDate = DateTypeHelper.parseDate("2002-10-10T00:00:00+05:00", formats);
    assertThat(parsedDate, isPresentAnd(isDay(2002, Month.OCTOBER, 10)));

    parsedDate = DateTypeHelper.parseDate("1999", formats);
    assertThat(parsedDate, isPresentAnd(isYear(1999)));
  }
}