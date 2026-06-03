package de.staatsbibliothek.berlin.hsp.fo.indexer.common;

import java.util.List;

/**
 * Provides Solr related constants
 */
public class Constants {

  public static final String FIELD_NAME_ID = "id-search";
  public static final String FIELD_NAME_GROUP_ID = "group-id-search";
  public static final String FIELD_NAME_TYPE = "type-search";
  public static final List<String> WHITELIST_FIELD_TYPES = List.of("binary", "boolean", "booleans", "ignored", "pdate", "pdates", "pdouble", "pdoubles", "pfloat", "pfloats", "pint", "pints", "plong", "plongs", "point", "random", "string", "strings", "text_general");
  public static final List<String> WHITELIST_FIELDS = List.of("_version_", "id");

  private Constants() {
  }
}
