package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.xml;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.XMLSource;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.model.FieldProcessingUnit;

import java.util.List;

@FunctionalInterface
public interface PostProcessingHandler {
  List<String> execute(final List<FieldProcessingUnit> postProcessors, final XMLSource node, final String value);
}
