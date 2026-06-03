package de.staatsbibliothek.berlin.hsp.fo.indexer.testutil;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.model.ClassField;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.model.FieldSelector;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.model.FieldSource;

import java.util.List;

public class TestDataFactory {
  public static ClassField createClassField(final String xPath, final boolean isMultiValue) {
    return ClassField
        .builder()
        .withSources(List.of(createFieldSource(xPath, isMultiValue)))
        .build();
  }

  public static FieldSource createFieldSource(final String xPath, final boolean isMultiValue) {
    return FieldSource
        .builder()
        .withFieldSelectors(List.of(createFieldSelector(xPath, isMultiValue)))
        .build();
  }

  public static FieldSelector createFieldSelector(final String xPath, final boolean isMultiValue) {
    return FieldSelector
        .builder()
        .withIsMultiValue(isMultiValue)
        .withXPath(xPath)
        .build();
  }
}
