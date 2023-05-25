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

  @XMLSource(selectors = @Selector(xPath = "/tei:bibl/tei:date[@type = 'digitalized']/@when"))
  @Field("digitization-date-display")
  private Date digitizationDateDisplay;

  @XMLSource(selectors = @Selector(xPath = "/tei:bibl/tei:placeName"))
  @Field("digitization-place-display")
  private String digitizationPlaceDisplay;

  @XMLSource(selectors = @Selector(xPath = "/tei:bibl/tei:orgName"))
  @Field("digitization-organization-display")
  private String digitizationOrganizationDisplay;

  @XMLSource(selectors = @Selector(xPath = "/tei:bibl/tei:date[@type = 'issued']/@when"))
  @Field("issuing-date-display")
  private Date issuingDateDisplay;

  @XMLSource(selectors = @Selector(xPath = "/tei:bibl/tei:ref[@type = 'manifest']/@subtype | /tei:bibl/tei:ref[@type = 'other']/@subtype"))
  @Field("subtype-display")
  private String subTypeDisplay;

  @XMLSource(selectors = @Selector(xPath = "/tei:bibl/tei:ref[@type = 'manifest']/@target"))
  @Field("manifest-uri-display")
  private String manifestURIDisplay;

  @XMLSource(selectors = @Selector(xPath = "/tei:bibl/tei:ref[@type = 'other']/@target"))
  @Field("external-uri-display")
  private String externalURIDisplay;

  @XMLSource(selectors = @Selector(xPath = "/tei:bibl/tei:ref[@type = 'thumbnail']/@target"))
  @Field("thumbnail-uri-display")
  private String thumbnailURIDisplay;

  @Field("type-search")
  private String typeSearch = "hsp:digitized";

  @Field("group-id-search")
  private String groupIdSearch;
}
