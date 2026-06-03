package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder(setterPrefix = "with")
@Data
public class FieldSource {

  /* whether the field's source content should be distinct or not */
  private boolean distinct;

  /* the source's fieldSelectors */
  private List<FieldSelector> fieldSelectors;
}
