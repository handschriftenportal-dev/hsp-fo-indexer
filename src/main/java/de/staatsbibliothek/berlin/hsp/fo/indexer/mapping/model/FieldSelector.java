package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder(setterPrefix = "with")
@Data
public class FieldSelector {
  /* xPath expression to map an additional value */
  private String additionalValue;

  /* the name of an attribute to map */
  private String attribute;

  /**
   * default value if xPath evaluation and post-processing results in an empty value
   * This can either be an attribute's name of the xPath() referenced element or `text()` to use the elements text node.
   * */
  private String defaultValue;

  /** if xPath evaluation results in multiple values and all of them should be used, isMultiValue should be true.
   * Otherwise, only the first value is considered
   */
  private boolean isMultiValue;

  /**
   * One or more Processing units, that should be used to post process the resulting value
   */
  private List<FieldProcessingUnit> fieldProcessingUnits;

  /**
   * A valid xPath expression that should be used to parse one or many values from a given TEI document.
   */
  private String xPath;
}
