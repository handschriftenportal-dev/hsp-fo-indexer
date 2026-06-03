package de.staatsbibliothek.berlin.hsp.fo.indexer.common.util;

import org.springframework.lang.NonNull;

public class StringHelper {

  private StringHelper() {}
  /**
   * removes leading and trailing and multiple inline whitespaces
   *
   * @param term term from which the whitespaces should be removed
   * @return the resulting term without leading, trailing and multiple inline whitspaces
   */
  public static String collapseAndTrim(@NonNull final String term) {
    return collapseSpaces(term).trim();
  }

  /**
   * Replaces all multiple occurrences of whitespaces by a single
   * @param term
   * @return
   */
  public static String collapseSpaces(@NonNull final String term) {
    return term.replaceAll("\\s+", " ");
  }
}
