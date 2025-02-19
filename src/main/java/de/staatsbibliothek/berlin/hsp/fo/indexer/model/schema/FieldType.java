package de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldType {

  @JsonProperty("class")
  @NonNull
  private String className;

  @JsonInclude(Include.NON_NULL)
  private String docValues;

  @JsonInclude(Include.NON_NULL)
  private String enumsConfig;

  @JsonInclude(Include.NON_NULL)
  private String enumName;

  @JsonInclude(Include.NON_NULL)
  private Analyzer indexAnalyzer;

  @JsonInclude(Include.NON_NULL)
  private Analyzer multiTermAnalyzer;

  @JsonProperty("multiValued")
  private boolean multiValued;

  @JsonProperty("name")
  @NonNull
  private String name;

  @JsonProperty("positionIncrementGap")
  private String positionIncrementGap;

  @JsonInclude(Include.NON_NULL)
  private Analyzer queryAnalyzer;

  @JsonProperty("sortMissingLast")
  private Boolean sortMissingLast;
}
