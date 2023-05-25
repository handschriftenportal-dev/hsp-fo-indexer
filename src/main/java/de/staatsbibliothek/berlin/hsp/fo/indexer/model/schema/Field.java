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

  @NonNull
  private String name;

  @NonNull
  private String type;

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
}
