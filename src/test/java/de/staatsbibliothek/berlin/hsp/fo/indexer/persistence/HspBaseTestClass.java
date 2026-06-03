package de.staatsbibliothek.berlin.hsp.fo.indexer.persistence;

import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspBase;
import lombok.*;
import org.apache.solr.client.solrj.beans.Field;

@AllArgsConstructor
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class HspBaseTestClass implements HspBase {
  @NonNull
  @Field("id-search")
  private String id;

  @NonNull
  @Field("type-search")
  private String typeSearch;

  @Field("group-id-search")
  private String groupId;
}
