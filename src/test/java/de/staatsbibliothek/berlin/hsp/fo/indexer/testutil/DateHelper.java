package de.staatsbibliothek.berlin.hsp.fo.indexer.testutil;

import java.util.Date;

public class DateHelper {

  public static boolean compareFrowzy(final Date date1, final Date date2) {
    return date1.getYear() == date2.getYear() && date1.getMonth() == date2.getMonth() && date1.getDay() == date2.getDay();
  }
}
