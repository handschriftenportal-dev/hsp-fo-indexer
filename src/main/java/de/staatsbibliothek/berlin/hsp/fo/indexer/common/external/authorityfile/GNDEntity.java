package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile;

import lombok.*;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * entity class for mapping an authority file
 */
@AllArgsConstructor
@Builder(setterPrefix = "with", access = AccessLevel.PUBLIC)
@Data
@NoArgsConstructor
public class GNDEntity {

  private String gndId;

  private String id;

  private Identifier[] identifier;

  private String name;

  private String preferredName;

  private Variant[] variantName;

  public List<String> getAsList() {
    final List<String> values = new ArrayList<>();
    CollectionUtils.addIgnoreNull(values, gndId);
    CollectionUtils.addIgnoreNull(values, id);
    CollectionUtils.addIgnoreNull(values, name);
    CollectionUtils.addIgnoreNull(values, preferredName);
    if (variantName != null) {
      Arrays.stream(variantName)
          .filter(Objects::nonNull)
          .forEach(vn -> CollectionUtils.addIgnoreNull(values, vn.getName()));
    }
    if (identifier != null) {
      Arrays.stream(identifier)
          .filter(Objects::nonNull)
          .forEach(identifierEntry -> CollectionUtils.addIgnoreNull(values, identifierEntry.getText()));
    }
    return values;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Variant {

    private String name;

    private String languageCode;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @ToString(includeFieldNames = false)
  public static class Identifier {

    private String text;

    private String type;

    private String url;
  }
}
