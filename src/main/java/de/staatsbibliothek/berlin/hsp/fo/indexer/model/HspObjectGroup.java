package de.staatsbibliothek.berlin.hsp.fo.indexer.model;

import lombok.Data;

import java.util.List;

@Data
public class HspObjectGroup {

  private HspObject hspObject;

  private List<HspDescription> hspDescriptions;

  private List<HspDigitized> hspDigitized;
}
