package de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents Solr field type information for analyzers
 */
@Data
@NoArgsConstructor
public class Analyzer {
  private Map<String, Object>[] charFilters;
  private Map<String, Object>[] filters;
  private Map<String, Object> tokenizer;
}
