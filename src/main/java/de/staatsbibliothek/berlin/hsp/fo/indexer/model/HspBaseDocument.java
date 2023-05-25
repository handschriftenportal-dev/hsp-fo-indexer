package de.staatsbibliothek.berlin.hsp.fo.indexer.model;

import lombok.Data;
import org.apache.solr.client.solrj.beans.Field;

@Data
public abstract class HspBaseDocument implements HspBase {

  @Field("described-object-facet")
  private String describedObjectFacet;

  @Field("digitized-object-facet")
  private String digitizedObjectFacet;

  @Field("digitized-iiif-object-facet")
  private String digitizedIiifObjectFacet;

  @Field("group-id-search")
  private String groupIdSearch;

  @Field("ms-identifier-search")
  private String msIdentifierSearch;

  @Field("ms-identifier-sort")
  private String msIdentifierSort;

  public abstract String getSettlementDisplay();
  public abstract String getRepositoryDisplay();
  public abstract String getIdnoSearch();
}