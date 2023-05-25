package de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema;

import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspBase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.solr.client.solrj.beans.Field;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class SchemaVersion implements HspBase {

  public static final String VERSIONING_DOCUMENT_ID = "VERSIONING_DOCUMENT_ID";

  @Field("version-schema")
  private String currentVersion;
  @Field("id")
  private String id;

  public SchemaVersion(final String currentVersion) {
    this.currentVersion = currentVersion;
    this.id = VERSIONING_DOCUMENT_ID;
  }
}
