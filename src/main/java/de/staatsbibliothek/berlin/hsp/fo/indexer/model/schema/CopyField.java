package de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema;

import lombok.*;

/**
 *
 */
@Data
@Builder
@NoArgsConstructor
@RequiredArgsConstructor
public class CopyField {

  @NonNull
  private String source;

  @NonNull
  private String dest;
}
