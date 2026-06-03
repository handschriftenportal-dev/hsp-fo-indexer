package de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 *
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor()
public class Field {

  @JsonInclude(Include.NON_NULL)
  @JsonProperty("default")
  private String defaultValue;

  @JsonProperty("stored")
  private boolean isStored;

  @JsonProperty("indexed")
  private boolean isIndexed;

  @JsonProperty("multiValued")
  private boolean isMultiValue;

  @JsonProperty("docValues")
  @JsonInclude(Include.NON_NULL)
  /*
   * Boolean type is imperative; primitive boolean would default to docValues=false which (beside
   * other possible problems) lead to empty id keys in highlighting results.
   */
  private Boolean isDocValues;

  @NonNull
  private String name;

  private boolean required;

  @NonNull
  private String type;
}
