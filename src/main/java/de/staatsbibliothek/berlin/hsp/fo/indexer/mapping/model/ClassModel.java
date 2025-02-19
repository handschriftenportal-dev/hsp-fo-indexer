package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder(setterPrefix = "with")
@Data
public class ClassModel {
  /* all the accessible fields of the class */
  private List<ClassField> fields;

  /* the full qualified name of the class */
  private String name;

  /* the fields type */
  private Class<?> type;
}
