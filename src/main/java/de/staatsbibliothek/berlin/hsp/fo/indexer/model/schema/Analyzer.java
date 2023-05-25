package de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 *
 */
@Data
@NoArgsConstructor
public class Analyzer {
  private Map<String, Object> tokenizer;

  private Map<String, Object>[] filters;
}
