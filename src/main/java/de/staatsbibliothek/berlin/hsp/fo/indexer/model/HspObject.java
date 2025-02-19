package de.staatsbibliothek.berlin.hsp.fo.indexer.model;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.ProcessingUnit;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.Selector;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.XMLSource;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ResultMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes.CorporatePostProcessor;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes.PlacePostProcessor;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes.YearPostProcessor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.solr.client.solrj.beans.Field;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class HspObject extends HspBaseDocument {

  @XMLSource(selectors =
      {
          @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:settlement/@key"),
          @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:altIdentifier[@type='former']/tei:settlement/@key"),
          @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:repository/@key"),
          @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:altIdentifier[@type='former']/tei:repository/@key"),
          @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_origPlace']/tei:term[@type='origPlace_norm']/@key"),
          @Selector(xPath = "//tei:persName/@key")
      })
  @Field("authority-file-facet")
  private String[] authorityFileFacet;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_dimensions']/tei:term[@type='depth']", isMultiValue = true))
  @Field("depth-facet")
  private Float[] depthFacet;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_dimensions']/tei:term[@type='dimensions']", isMultiValue = true))
  @Field("dimensions-display")
  private String[] dimensionsDisplay;

  @Field("format-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_format']/tei:term[@type='format']", isMultiValue = true))
  private String[] formatFacet;

  @Field("format-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_format']/tei:term[@type='format']", isMultiValue = true))
  private String[] formatSearch;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_format']/tei:term[@type='format_typeOfInformation']", isMultiValue = true))
  @Field("format-type-display")
  private String[] formatTypeDisplay;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_format']/tei:term[@type='format_typeOfInformation']", isMultiValue = true))
  @Field("format-type-facet")
  private String[] formatTypeFacet;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:altIdentifier[@type='former']/tei:idno"))
  private String[] formerIdnoSearch;

  @Field("former-ms-identifier-search")
  private String[] formerMsIdentifierSearch;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:altIdentifier[@type='former']/tei:settlement", attribute = "key", isMultiValue = true, processingUnits = @ProcessingUnit(processorClass = PlacePostProcessor.class, mapper = ResultMapper.PREFERRED_NAME), defaultValue = "text()"))
  private String[] formerSettlementSearch;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_musicNotation']/tei:term[@type='musicNotation']"))
  @Field("has-notation-display")
  private String hasNotationDisplay;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_musicNotation']/tei:term[@type='musicNotation']"))
  @Field("has-notation-facet")
  private String[] hasNotationFacet;

  @Field("height-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_dimensions']/tei:term[@type='height']", isMultiValue = true))
  private Float[] heightFacet;

  @Field("height-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_dimensions']/tei:term[@type='height']", isMultiValue = true))
  private Float[] heightSearch;

  @Field("id")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/@xml:id"))
  private String id;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:altIdentifier/tei:idno", isMultiValue = true))
  @Field("idno-alternative-search")
  private String[] idnoAltSearch;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:idno"))
  @Field("idno-search")
  private String idnoSearch;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:idno/@sortKey"))
  private String idnoSortKey;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_decoration']/tei:term[@type='decoration']"))
  @Field("illuminated-display")
  private String illuminatedDisplay;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_decoration']/tei:term[@type='decoration']"))
  @Field("illuminated-facet")
  private String[] illuminatedFacet;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:altIdentifier[@type='former']/tei:repository", attribute = "key", isMultiValue = true, processingUnits = @ProcessingUnit(processorClass = CorporatePostProcessor.class, mapper = ResultMapper.PREFERRED_NAME), defaultValue = "text()"))
  @Field("institution-previously-owning-search")
  private String[] institutionPreviouslyOwningSearch;

  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_textLang']/tei:term[@type='textLang-ID']", isMultiValue = true))
  @Field("language-display")
  private String[] languageDisplay;

  @Field("language-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_textLang']/tei:term[@type='textLang-ID']", isMultiValue = true))
  private String[] languageFacet;

  @Field("language-search")
  @XMLSource(selectors = {
      @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_textLang']/tei:term[@type='textLang-ID']", isMultiValue = true),
      @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_textLang']/tei:term[@type='textLang']", isMultiValue = true),
      @Selector(xPath = "//tei:textLang", isMultiValue = true),
      @Selector(xPath = "//tei:textLang/@mainLang", isMultiValue = true),
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
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_measure']/tei:term[@type='measure_noOfLeaves']"))
  private Integer[] leavesCountFacet;

  @Field("leaves-count-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_measure']/tei:term[@type='measure_noOfLeaves']"))
  private Integer[] leavesCountSearch;

  @Field("leaves-count-display")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_measure']/tei:term[@type='measure']"))
  private String leavesCountDisplay;

  @Field("material-search")
  @XMLSource(selectors = {@Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_material']/tei:term[@type='material']"), @Selector(xPath = "//tei:msDesc/tei:head/tei:index[@indexName='norm_material']/tei:term[@type='material_type']", isMultiValue = true)})
  private String[] materialSearch;

  @Field("material-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_material']/tei:term[@type='material_type']", isMultiValue = true))
  private String[] materialFacet;

  @Field("material-display")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_material']/tei:term[@type='material']"))
  private String materialDisplay;

  @Field("object-type-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_form']/tei:term[@type='form']"))
  private String[] objectTypeFacet;

  @Field("object-type-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_form']/tei:term[@type='form']"))
  private String objectTypeSearch;

  @Field("orig-date-from-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_origDate']/tei:term[@type='origDate_notBefore']", isMultiValue = true))
  private Integer[] origDateFromFacet;

  @Field("orig-date-from-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_origDate']/tei:term[@type='origDate_notBefore']", isMultiValue = true, processingUnits = @ProcessingUnit(processorClass = YearPostProcessor.class)))
  private Integer[] origDateFromSearch;

  @Field("orig-date-from-sort")
  private Integer origDateFromSort;

  @Field("orig-date-to-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_origDate']/tei:term[@type='origDate_notAfter']", isMultiValue = true))
  private Integer[] origDateToFacet;

  @Field("orig-date-to-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_origDate']/tei:term[@type='origDate_notAfter']", isMultiValue = true,  processingUnits = @ProcessingUnit(processorClass = YearPostProcessor.class)))
  private Integer[] origDateToSearch;

  @Field("orig-date-to-sort")
  private Integer origDateToSort;

  @Field("orig-date-type-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_origDate']/tei:term[@type='origDate_type']", isMultiValue = true))
  private String[] origDateTypeFacet;

  @Field("orig-date-lang-display")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_origDate']/tei:term[@type='origDate']", isMultiValue = true))
  private String[] origDateLangDisplay;

  @Field("orig-date-when-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_origDate']/tei:term[@type='origDate' and ../tei:term[@type='origDate_type']/text() = 'dated']", isMultiValue = true))
  private Integer[] origDateWhenFacet;

  @Field("orig-date-when-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_origDate']/tei:term[@type='origDate']", isMultiValue = true, processingUnits = @ProcessingUnit(processorClass = YearPostProcessor.class)))
  private Integer[] origDateWhenSearch;

  @Field("orig-place-display")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_origPlace']/tei:term[@type='origPlace']"))
  private String origPlaceDisplay;

  @Field("orig-place-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_origPlace']/tei:term[@type='origPlace_norm']", attribute = "key", isMultiValue = true, processingUnits = @ProcessingUnit(processorClass = PlacePostProcessor.class, mapper = ResultMapper.PREFERRED_NAME), defaultValue = "text()"))
  private String[] origPlaceFacet;

  @Field("orig-place-authority-file-display")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_origPlace']/tei:term[@type='origPlace_norm']", attribute = "key", isMultiValue = true))
  private String[] origPlaceAuthorityFileDisplay;

  @Field("orig-place-search")
  @XMLSource(selectors = {
      @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_origPlace']/tei:term[@type='origPlace']"),
      @Selector(xPath = "//tei:msDesc/tei:head/tei:index[@indexName='norm_origPlace']/tei:term[@type='origPlace_norm']", isMultiValue = true, attribute = "key", processingUnits = @ProcessingUnit(processorClass = PlacePostProcessor.class), additionalValue = "text()")})
  private String[] origPlaceSearch;

  @Field("persistent-url-display")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:pubPlace/tei:ptr[@type='purl' and @subtype='hsp']/@target"))
  private String persistentURLDisplay;

  @Field("repository-authority-file-display")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc[@type='hsp:object']/tei:msIdentifier/tei:repository", attribute = "key", isMultiValue = true))
  private String[] repositoryAuthorityFileDisplay;

  @Field("repository-display")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:repository", attribute = "key", processingUnits = @ProcessingUnit(processorClass = CorporatePostProcessor.class, mapper = ResultMapper.PREFERRED_NAME), defaultValue = "text()"))
  private String repositoryDisplay;

  @Field("repository-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:repository", attribute = "key", processingUnits = @ProcessingUnit(processorClass = CorporatePostProcessor.class, mapper = ResultMapper.PREFERRED_NAME)))
  private String[] repositoryFacet;

  @Field("repository-id-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:repository/@key"))
  private String repositoryIdFacet;

  @Field("repository-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:repository", attribute = "key", processingUnits = @ProcessingUnit(processorClass = CorporatePostProcessor.class), additionalValue = "text()"))
  private String[] repositorySearch;

  @Field("settlement-display")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:settlement", attribute = "key", processingUnits = @ProcessingUnit(processorClass = PlacePostProcessor.class, mapper = ResultMapper.PREFERRED_NAME), defaultValue = "text()"))
  private String settlementDisplay;

  @Field("settlement-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:settlement", attribute = "key", processingUnits = @ProcessingUnit(processorClass = PlacePostProcessor.class, mapper = ResultMapper.PREFERRED_NAME)))
  private String[] settlementFacet;

  @Field("settlement-authority-file-display")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:settlement", attribute = "key", isMultiValue = true))
  private String[] settlementAuthorityFileDisplay;

  @Field("settlement-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:settlement", attribute = "key", processingUnits = @ProcessingUnit(processorClass = PlacePostProcessor.class), additionalValue = "text()"))
  private String[] settlementSearch;

  @Field("status-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_status']/tei:term[@type='status']"))
  private String[] statusFacet;

  @Field(value = "status-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_status']/tei:term[@type='status']"))
  private String statusSearch;

  @Field(value = "tei-document-display")
  private String teiDocumentDisplay;

  @Field(value = "title-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_title']/tei:term[@type='title']"))
  private String titleSearch;

  @Field(value = "type-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/@type"))
  private String typeSearch;

  @Field(value = "width-facet")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_dimensions']/tei:term[@type='width']", isMultiValue = true))
  private Float[] widthFacet;

  @Field(value = "width-search")
  @XMLSource(selectors = @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:head/tei:index[@indexName='norm_dimensions']/tei:term[@type='width']", isMultiValue = true))
  private Float[] widthSearch;
}
