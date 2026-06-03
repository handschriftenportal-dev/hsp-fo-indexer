package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile;

import lombok.Getter;

/**
 *
 */
@Getter
public enum GNDEntityType {

  CORPORATE("CorporateBody"),
  PERSON("Person"),
  PLACE("Place");

  private final String type;

  GNDEntityType(final String type) {
    this.type = type;
  }
}
