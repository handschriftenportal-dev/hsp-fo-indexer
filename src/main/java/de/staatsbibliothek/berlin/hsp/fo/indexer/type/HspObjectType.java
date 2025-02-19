package de.staatsbibliothek.berlin.hsp.fo.indexer.type;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Provides types for all HSP entities
 */
public enum HspObjectType {
  CATALOG("hsp:catalog"),
  DESCRIPTION("hsp:description", "hsp:description_retro"),
  DIGITIZATION("hsp:digitized"),
  OBJECT("hsp:object");

  private final String[] value;

  HspObjectType(final String... value) {
    this.value = value;
  }

  public String[] getValue() {
    return value;
  }

  public boolean equalsValue(final String valueToCompare) {
    return ArrayUtils.contains(this.value, valueToCompare);
  }
}
