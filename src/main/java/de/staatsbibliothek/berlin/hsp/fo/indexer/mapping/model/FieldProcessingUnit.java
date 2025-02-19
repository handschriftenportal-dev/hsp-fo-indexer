package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.model;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ResultMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes.IAttributePostProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder(setterPrefix = "with")
@Data
public class FieldProcessingUnit {
  private Class<? extends IAttributePostProcessor> processorClass;

  private ResultMapper resultMapper;
}
