package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile;

/**
 *
 */
public enum GNDEntityType {

  CORPORATE("CorporateBody"),
  LANGUAGE("language"),
  PERSON("Person"),
  PLACE("Place");

  private final String type;

  GNDEntityType(final String type) {
    this.type = type;
  }

  public String getType() {
    return this.type;
  }
}
