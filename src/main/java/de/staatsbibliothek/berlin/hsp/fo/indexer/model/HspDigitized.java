package de.staatsbibliothek.berlin.hsp.fo.indexer.model;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.Selector;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.XMLSource;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.solr.client.solrj.beans.Field;

import java.util.Date;

@Data
@NoArgsConstructor
public class HspDigitized implements HspBase {

  @XMLSource(selectors = @Selector(xPath = "/tei:bibl/tei:idno"))
  @Field("id")
  private String id;

  @XMLSource(selectors = @Selector(xPath = "/tei:bibl/tei:date[@type = 'digitized']/@when"))
  @Field("digitization-date-display")
  private Date digitizationDateDisplay;

  @XMLSource(selectors = @Selector(xPath = "/tei:bibl/tei:orgName[@type = 'digitizing']"))
  @Field("digitization-institution-display")
  private String digitizationInstitutionDisplay;

  @XMLSource(selectors = @Selector(xPath = "/tei:bibl/tei:placeName[@type = 'digitizing']"))
  @Field("digitization-settlement-display")
  private String digitizationSettlementDisplay;

  @XMLSource(selectors = @Selector(xPath = "/tei:bibl/tei:ref[@type = 'other']/@target"))
  @Field("external-uri-display")
  private String externalURIDisplay;

  @Field("group-id-search")
  private String groupIdSearch;

  @XMLSource(selectors = @Selector(xPath = "/tei:bibl/tei:date[@type = 'issued']/@when"))
  @Field("issuing-date-display")
  private Date issuingDateDisplay;

  @Field("kod-id-display")
  private String kodIdDisplay;

  @XMLSource(selectors = @Selector(xPath = "/tei:bibl/tei:ref[@type = 'manifest']/@target"))
  @Field("manifest-uri-search")
  private String manifestURISearch;

  @Field("repository-id-facet")
  private String repositoryIdFacet;

  // de-activated until value is provided correctly
  // @XMLSource(selectors = @FieldSelector(xPath = "/tei:bibl/tei:ref[@type = 'manifest']/@subtype | /tei:bibl/tei:ref[@type = 'other']/@subtype"))
  @Field("subtype-display")
  private String subTypeDisplay;

  @XMLSource(selectors = @Selector(xPath = "/tei:bibl/tei:ref[@type = 'thumbnail']/@target"))
  @Field("thumbnail-uri-display")
  private String thumbnailURIDisplay;

  @Field("type-search")
  private String typeSearch = "hsp:digitized";
}
