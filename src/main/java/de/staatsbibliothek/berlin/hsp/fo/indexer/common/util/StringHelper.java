package de.staatsbibliothek.berlin.hsp.fo.indexer.common.util;

public class StringHelper {

  private StringHelper() {}
  /**
   * removes leading and trailing and multiple inline whitespaces
   *
   * @param term term from which the whitespaces should be removed
   * @return the resulting term without leading, trailing and multiple inline whitspaces
   */
  public static String removeWhitespaces(final String term) {
    if (term != null && !term.isBlank()) {
      return term.trim()
              .replaceAll("\\s+", " ");
    } else {
      return null;
    }
  }
}
