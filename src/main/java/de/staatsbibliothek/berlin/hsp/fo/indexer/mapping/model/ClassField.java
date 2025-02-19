package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder(setterPrefix = "with")
@Data
public class ClassField {
  /* the fields name */
  private String name;

  /* the fields type */
  private Class<?> type;

  /* the content's source */
  private List<FieldSource> sources;
}
