package de.staatsbibliothek.berlin.hsp.fo.indexer.model;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.ProcessingUnit;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.Selector;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.XMLSource;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ResultMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes.CorporatePostProcessor;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes.PlacePostProcessor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.solr.client.solrj.beans.Field;

import java.util.Date;

/**
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HspDescription extends HspBaseDocument {
  @XMLSource(selectors = @Selector(xPath = "//tei:msPart[@type='accMat']//*", isMultiValue = true), distinct = false)
  @Field("accompanying-material-search")
  private String accompanyingMaterialSearch;

  @Field("binding-orig-place-search")
  @XMLSource(selectors = {@Selector(xPath = "//tei:msPart[@type='binding']/tei:head/tei:index/tei:term[@type='origPlace']"), @Selector(xPath = "//tei:msPart[@type='binding']/tei:head/tei:index/tei:term[@type='origPlace_gnd-ID']", isMultiValue = true)})
  private String[] bindingOrigPlaceSearch;

  @XMLSource(selectors = @Selector(xPath = "//tei:msPart[@type='binding']//*", isMultiValue = true), distinct = false)
  @Field("binding-search")
  private String bindingSearch;

  @XMLSource(selectors = @Selector(xPath = "//tei:msPart[@type='booklet']//*", isMultiValue = true), distinct = false)
  @Field("booklet-search")
  private String bookletSearch;

  @Field("catalog-id-display")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:pubPlace/tei:ptr[@type='hsp']/@target"))
  private String catalogIdDisplay;

  @Field("catalog-iiif-manifest-range-url-display")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:bibl/tei:biblScope/@facs"))
  private String catalogIIIFManifestRangeUrlDisplay;

  @Field("catalog-iiif-manifest-url-display")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:bibl/tei:ptr[@type='hsp']/@target"))
  private String catalogIIIFManifestUrlDisplay;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:msIdentifier/tei:collection"))
  @Field("collection-search")
  private String collectionSearch;

  @XMLSource(selectors = {@Selector(xPath = "//tei:physDesc/tei:decoDesc/tei:p", isMultiValue = true), @Selector(xPath = "//tei:msContents/tei:msItem/tei:decoNote[@type='form']", isMultiValue = true),}, distinct = false)
  @Field("decoration-search")
  private String decorationSearch;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_dimensions']/tei:term[@type='depth']", isMultiValue = true))
  @Field("depth-facet")
  private Float[] depthFacet;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:respStmt/tei:persName[contains(@role, 'author')]", isMultiValue = true))
  @Field("desc-author-search")
  private String[] descAuthorSearch;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:date[@type='modified']/@when"))
  @Field("desc-publish-date-search")
  private Date descPublishDateSearch;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_dimensions']/tei:term[@type='dimensions']", isMultiValue = true))
  @Field("dimensions-display")
  private String[] dimensionsDisplay;

  @XMLSource(selectors = @Selector(xPath = "//tei:quote[@type='explicit']", isMultiValue = true))
  @Field("explicit-search")
  private String[] explicitSearch;

  @Field("format-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_format']/tei:term[@type='format']", isMultiValue = true))
  private String[] formatFacet;

  @Field("format-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_format']/tei:term[@type='format']", isMultiValue = true))
  private String[] formatSearch;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_format']/tei:term[@type='format_typeOfInformation']", isMultiValue = true))
  @Field("format-type-display")
  private String[] formatTypeDisplay;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_format']/tei:term[@type='format_typeOfInformation']", isMultiValue = true))
  @Field("format-type-facet")
  private String[] formatTypeFacet;

  @XMLSource(selectors = @Selector(xPath = "//tei:msPart[@type='fragment']//*", isMultiValue = true), distinct = false)
  @Field("fragment-search")
  private String fragmentSearch;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]//*[not(parent::tei:index)]/text()", isMultiValue = true), distinct = false)
  @Field("fulltext-search")
  private String fulltextSearch;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_musicNotation']/tei:term[@type='musicNotation']"))
  @Field("has-notation-display")
  private String hasNotationDisplay;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_musicNotation']/tei:term[@type='musicNotation']"))
  @Field("has-notation-facet")
  private String[] hasNotationFacet;

  @Field("height-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_dimensions']/tei:term[@type='height']", isMultiValue = true))
  private Float[] heightFacet;

  @Field("history-search")
  @XMLSource(selectors = @Selector(xPath = "//tei:history/descendant-or-self::*", isMultiValue = true))
  private String historySearch;

  @Field("id")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/@xml:id"))
  private String id;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:msIdentifier/tei:idno"))
  @Field("idno-search")
  private String idnoSearch;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:msIdentifier/tei:idno/@sortKey"))
  private String idnoSortKey;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:msIdentifier/tei:altIdentifier/tei:idno", isMultiValue = true))
  @Field("idno-alternative-search")
  private String[] idnoAltSearch;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_decoration']/tei:term[@type='decoration']"))
  @Field("illuminated-display")
  private String illuminatedDisplay;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_decoration']/tei:term[@type='decoration']"))
  @Field("illuminated-facet")
  private String[] illuminatedFacet;

  @XMLSource(selectors = @Selector(xPath = "//tei:quote[@type='incipit']", isMultiValue = true))
  @Field("incipit-search")
  private String[] incipitSearch;

  @XMLSource(selectors = @Selector(xPath = "//tei:msContents/tei:msItem/tei:decoNote[@type='content']/descendant-or-self::*/text()", isMultiValue = true), distinct = false)
  @Field("item-iconography-search")
  private String itemIconographySearch;

  @XMLSource(selectors = @Selector(xPath = "//tei:msContents/tei:msItem/tei:note[@type='music']/descendant-or-self::*/text()", isMultiValue = true), distinct = false)
  @Field("item-music-search")
  private String itemMusicSearch;

  @XMLSource(selectors = @Selector(xPath = "//tei:msContents/tei:msItem/tei:note[@type='text']/descendant-or-self::*/text()", isMultiValue = true), distinct = false)
  @Field("item-text-search")
  private String itemTextSearch;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_textLang']/tei:term[@type='textLang-ID']", isMultiValue = true))
  @Field("language-display")
  private String[] languageDisplay;

  @Field("language-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_textLang']/tei:term[@type='textLang-ID']", isMultiValue = true))
  private String[] languageFacet;

  @Field("language-search")
  @XMLSource(selectors = {
          @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_textLang']/tei:term[@type='textLang-ID']", isMultiValue = true),
          @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_textLang']/tei:term[@type='textLang']", isMultiValue = true),
          @Selector(xPath = "//tei:textLang", isMultiValue = true),
          @Selector(xPath = "//tei:textLang/@otherLangs", isMultiValue = true),
          @Selector(xPath = "//tei:rubric/@lang", isMultiValue = true),
          @Selector(xPath = "//tei:incipit/@lang", isMultiValue = true),
          @Selector(xPath = "//tei:explicit/@lang", isMultiValue = true),
          @Selector(xPath = "//tei:colophon/@lang", isMultiValue = true),
          @Selector(xPath = "//tei:finalRubric/@lang", isMultiValue = true)

  })
  private String[] languageSearch;

  @Field("last-modified-display")
  @XMLSource(selectors = {
          @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:date[@type='issued']/@when"),
          @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:revisionDesc/tei:change/tei:date/@when")
  })
  private Date lastModifiedDisplay;

  @Field("leaves-count-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_measure']/tei:term[@type='measure_noOfLeaves']"))
  private Integer[] leavesCountFacet;

  @Field("leaves-count-display")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_measure']/tei:term[@type='measure']"))
  private String leavesCountDisplay;

  @Field("material-search")
  @XMLSource(selectors = {@Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_material']/tei:term[@type='material']"), @Selector(xPath = "//tei:msDesc/tei:head/tei:index[@indexName='norm_material']/tei:term[@type='material_type']", isMultiValue = true)})
  private String[] materialSearch;

  @Field("material-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_material']/tei:term[@type='material_type']", isMultiValue = true))
  private String[] materialFacet;

  @Field("material-display")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_material']/tei:term[@type='material']"))
  private String materialDisplay;

  @Field("object-type-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_form']/tei:term[@type='form']"))
  private String[] objectTypeFacet;

  @Field("object-type-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_form']/tei:term[@type='form']"))
  private String objectTypeSearch;

  @Field("orig-date-from-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_origDate']/tei:term[@type='origDate_notBefore']", isMultiValue = true))
  private Integer[] origDateFromFacet;

  @Field("orig-date-from-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_origDate']/tei:term[@type='origDate_notBefore']", isMultiValue = true))
  private Integer[] origDateFromSearch;

  @Field("orig-date-from-sort")
  private Integer origDateFromSort;

  @Field("orig-date-to-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_origDate']/tei:term[@type='origDate_notAfter']", isMultiValue = true))
  private Integer[] origDateToFacet;

  @Field("orig-date-to-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_origDate']/tei:term[@type='origDate_notAfter']", isMultiValue = true))
  private Integer[] origDateToSearch;

  @Field("orig-date-to-sort")
  private Integer origDateToSort;

  @Field("orig-date-type-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_origDate']/tei:term[@type='origDate_type']", isMultiValue = true))
  private String[] origDateTypeFacet;

  @Field("orig-date-when-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_origDate']/tei:term[@type='origDate' and ../tei:term[@type='origDate_type']/text() = 'dated']", isMultiValue = true))
  private Integer[] origDateWhenFacet;

  @Field("orig-date-when-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_origDate']/tei:term[@type='origDate']", isMultiValue = true))
  private String[] origDateWhenSearch;

  @Field("orig-place-display")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_origPlace']/tei:term[@type='origPlace']"))
  private String origPlaceDisplay;

  @Field("orig-place-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_origPlace']/tei:term[@type='origPlace_norm']", attribute = "key", isMultiValue = true, processingUnits = @ProcessingUnit(processorClass = PlacePostProcessor.class, mapper = ResultMapper.PREFERRED_NAME), defaultValue = "text()"))
  private String[] origPlaceFacet;

  @Field("persistent-url-display")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:pubPlace/tei:ptr[@type='purl' and @subtype='hsp']/@target"))
  private String persistentURLDisplay;

  @Field("orig-place-search")
  @XMLSource(selectors = {
          @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_origPlace']/tei:term[@type='origPlace']"),
          @Selector(xPath = "//tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_origPlace']/tei:term[@type='origPlace_norm']", isMultiValue = true, attribute = "key", processingUnits = @ProcessingUnit(processorClass = PlacePostProcessor.class), additionalValue = "text()")})
  private String[] origPlaceSearch;

  @Field("person-author-search")
  @XMLSource(selectors = @Selector(xPath = "//tei:persName[contains(@role, 'author')]", isMultiValue = true))
  private String[] personAuthorSearch;

  @Field("person-bookbinder-search")
  @XMLSource(selectors = @Selector(xPath = "//tei:persName[contains(@role, 'bookbinder')]", isMultiValue = true))
  private String[] personBookbinderSearch;

  @Field("person-commissioned-by-search")
  @XMLSource(selectors = @Selector(xPath = "//tei:persName[contains(@role, 'commissionedBy')]", isMultiValue = true))
  private String[] personCommissionedBySearch;

  @Field("person-illuminator-search")
  @XMLSource(selectors = @Selector(xPath = "//tei:persName[contains(@role, 'illuminator')]", isMultiValue = true))
  private String[] personIlluminatorSearch;

  @Field("person-mentioned-in-search")
  @XMLSource(selectors = @Selector(xPath = "//tei:persName[contains(@role, 'mentionedIn')]", isMultiValue = true))
  private String[] personMentionedInSearch;

  @Field("person-other-search")
  @XMLSource(selectors = @Selector(xPath = "//tei:persName[contains(@role, 'other')]", isMultiValue = true))
  private String[] personOtherSearch;

  @Field("person-previous-owner-search")
  @XMLSource(selectors = @Selector(xPath = "//tei:persName[contains(@role, 'previousOwner')]", isMultiValue = true))
  private String[] personPreviousOwnerSearch;

  @Field("person-conservator-search")
  @XMLSource(selectors = @Selector(xPath = "//tei:persName[contains(@role, 'conservator')]", isMultiValue = true))
  private String[] personConservatorSearch;

  @Field("person-scribe-search")
  @XMLSource(selectors = @Selector(xPath = "//tei:persName[contains(@role, 'scribe')]", isMultiValue = true))
  private String[] personScribeSearch;

  @Field("person-translator-search")
  @XMLSource(selectors = @Selector(xPath = "//tei:persName[contains(@role, 'translator')]", isMultiValue = true))
  private String[] personTranslatorSearch;

  @XMLSource(selectors = @Selector(xPath = "//tei:physDesc/descendant-or-self::*[not(ancestor-or-self::tei:decoDesc)]/text()", isMultiValue = true), distinct = false)
  @Field("physical-description-search")
  private String physicalDescriptionSearch;

  @Field("repository-display")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:msIdentifier/tei:repository", attribute = "key", processingUnits = @ProcessingUnit(processorClass = CorporatePostProcessor.class, mapper = ResultMapper.PREFERRED_NAME), defaultValue = "text()"))
  private String repositoryDisplay;

  @Field("repository-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:msIdentifier/tei:repository", attribute = "key", processingUnits = @ProcessingUnit(processorClass = CorporatePostProcessor.class, mapper = ResultMapper.PREFERRED_NAME)))
  private String[] repositoryFacet;

  @Field("repository-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:msIdentifier/tei:repository", attribute = "key", processingUnits = @ProcessingUnit(processorClass = CorporatePostProcessor.class), additionalValue = "text()"))
  private String[] repositorySearch;

  @Field("settlement-display")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:msIdentifier/tei:settlement", attribute = "key", processingUnits = @ProcessingUnit(processorClass = PlacePostProcessor.class, mapper = ResultMapper.PREFERRED_NAME), defaultValue = "text()"))
  private String settlementDisplay;

  @Field("settlement-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:msIdentifier/tei:settlement", attribute = "key", processingUnits = @ProcessingUnit(processorClass = PlacePostProcessor.class, mapper = ResultMapper.PREFERRED_NAME)))
  private String[] settlementFacet;

  @Field("settlement-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:msIdentifier/tei:settlement", attribute = "key", processingUnits = @ProcessingUnit(processorClass = PlacePostProcessor.class), additionalValue = "text()"))
  private String[] settlementSearch;

  @Field("status-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_status']/tei:term[@type='status']"))
  private String[] statusFacet;

  @Field(value = "status-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_status']/tei:term[@type='status']"))
  private String statusSearch;

  @Field(value = "tei-document-display")
  private String teiDocumentDisplay;

  @Field(value = "title-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_title']/tei:term[@type='title']"))
  private String titleSearch;

  @Field(value = "type-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/@type"))
  private String typeSearch;

  @Field(value = "width-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:text/tei:body/tei:msDesc[starts-with(@type, 'hsp:description')]/tei:head/tei:index[@indexName='norm_dimensions']/tei:term[@type='width']", isMultiValue = true))
  private Float[] widthFacet;

  @Field(value = "work-title-search")
  @XMLSource(selectors = @Selector(xPath = "//tei:msPart/tei:head/tei:title", isMultiValue = true))
  private String[] workTitleSearch;
}