package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.entity;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.ProcessingUnit;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.Selector;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.XMLSource;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ResultMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes.CorporatePostProcessor;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes.PersonPostProcessor;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes.PlacePostProcessor;

public class MappingTestModel {

  @XMLSource(selectors = @Selector(xPath = "//tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:respStmt/tei:name", attribute = "type"))
  private String testAttrSingleSelector;

  @XMLSource(selectors = @Selector(xPath = "//tei:teiHeader/tei:fileDesc/tei:editionStmt/tei:respStmt/tei:name", attribute = "type", isMultiValue = true))
  private String testAttrSingleSelectorMulti;

  @XMLSource(selectors = {@Selector(xPath = "//tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:respStmt/tei:name", attribute = "type"), @Selector(xPath = "//tei:teiHeader/tei:fileDesc/tei:editionStmt/tei:respStmt/tei:name", attribute = "type", isMultiValue = true)})
  private String testAttrMultiSelector;

  @XMLSource(selectors = {@Selector(xPath = "//tei:msDesc/tei:head/tei:index[@indexName='norm']/tei:term[@type='foo']"), @Selector(xPath = "//tei:msDesc/tei:head/tei:index[@indexName='norm']/tei:term[@type='bar']")})
  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:physDesc/tei:objectDesc/tei:supportDesc/tei:extent/tei:dimensions[@type='leaf' and @unit='cm']/tei:height"))
  private String testAttr_multiNode_singleSelector_firstEmpty;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:head/tei:index[@indexName='norm']/tei:term[@type='height']"))
  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:physDesc/tei:objectDesc/tei:supportDesc/tei:extent/tei:dimensions[@type='leaf' and @unit='cm']/tei:height"))
  private String testAttr_multiNode_singleSelector;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:head/tei:index[@indexName='norm']/tei:term[@type='height']"))
  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:physDesc/tei:objectDesc/tei:supportDesc/tei:extent/tei:dimensions[@type='leaf' and @unit='cm']/tei:height"))
  private String testAttr_multiNode_singleSelector_prioritized;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:head/tei:index[@indexName='norm']/tei:term[@type='height'] | //tei:msDesc/tei:physDesc/tei:objectDesc/tei:supportDesc/tei:extent/tei:dimensions[@type='leaf' and @unit='cm']/tei:height", isMultiValue = true))
  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:physDesc/tei:objectDesc/tei:supportDesc/tei:extent/tei:dimensions[@type='leaf' and @unit='cm']/tei:width"))
  private String[] testAttr_multiNode_multiSelector_multiValue;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:msIdentifier/tei:repository", attribute = "key", processingUnits = @ProcessingUnit(processorClass = CorporatePostProcessor.class), additionalValue = "text()"))
  private String repositorySearch;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:msIdentifier/tei:repository", attribute = "key", processingUnits = @ProcessingUnit(processorClass = CorporatePostProcessor.class, mapper = ResultMapper.PREFERRED_NAME)))
  private String repositoryFacet;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:msIdentifier/tei:repository"))
  private String repositoryDisplay;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:msIdentifier/tei:settlement", attribute = "key", processingUnits = @ProcessingUnit(processorClass = PlacePostProcessor.class, mapper = ResultMapper.PREFERRED_NAME)))
  private String settlementFacet;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:msIdentifier/tei:settlement", attribute = "key", processingUnits = @ProcessingUnit(processorClass = PlacePostProcessor.class), additionalValue = "text()"))
  private String settlementSearch;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:head/tei:index[@indexName='norm_origPlace']/tei:term[@type='origPlace_gnd-ID']", isMultiValue = true, processingUnits = @ProcessingUnit(processorClass = PlacePostProcessor.class)))
  private String[] origPlaceSearch;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:head/tei:index[@indexName='norm_origPlace']/tei:term[@type='origPlace_gnd-ID']", isMultiValue = true, processingUnits = @ProcessingUnit(processorClass = PlacePostProcessor.class, mapper = ResultMapper.PREFERRED_NAME)))
  private String[] origPlaceFacet;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:head/tei:index[@indexName='norm_origPlace']/tei:term[@type='origPlace']", isMultiValue = true, processingUnits = @ProcessingUnit(processorClass = PlacePostProcessor.class, mapper = ResultMapper.PREFERRED_NAME)))
  private String[] origPlacePreferred;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:head/tei:index[@indexName='norm_origPlace']/tei:term[@type='origPlace']", isMultiValue = true, processingUnits = @ProcessingUnit(processorClass = PlacePostProcessor.class)))
  private String[] origPlace;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:head/tei:index[@indexName='norm_origPlace']/tei:term[@type='origPlace']", defaultValue = "text()", isMultiValue = true, processingUnits = @ProcessingUnit(processorClass = CorporatePostProcessor.class)))
  private String[] corporatePreferredPostProcessor;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:msIdentifier/tei:repository", attribute = "key", processingUnits = @ProcessingUnit(processorClass = CorporatePostProcessor.class)))
  private String corporatePostProcessorWithAuthorityFileReference;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:msIdentifier/tei:repository", attribute = "key", processingUnits = @ProcessingUnit(processorClass = CorporatePostProcessor.class, mapper = ResultMapper.PREFERRED_NAME)))
  private String corporatePreferredPostProcessorWithAuthorityFileReference;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:msIdentifier/tei:repository", processingUnits = @ProcessingUnit(processorClass = CorporatePostProcessor.class, mapper = ResultMapper.PREFERRED_NAME), defaultValue = "text()"))
  private String corporatePreferredPostProcessorWithoutAuthorityFileReferenceWithDefault;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:msIdentifier/tei:settlement", attribute = "key", processingUnits = @ProcessingUnit(processorClass = PlacePostProcessor.class, mapper = ResultMapper.PREFERRED_NAME)))
  private String placePreferredPostProcessorWithAuthorityFileReference;

  @XMLSource(selectors = @Selector(xPath = "//tei:publicationStmt/tei:publisher", attribute = "key", processingUnits = @ProcessingUnit(processorClass = PlacePostProcessor.class, mapper = ResultMapper.PREFERRED_NAME)))
  private String placePreferredPostProcessorWithoutAuthorityFileReference;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:msIdentifier/tei:settlement", attribute = "key", defaultValue = "text()", processingUnits = @ProcessingUnit(processorClass = PlacePostProcessor.class, mapper = ResultMapper.PREFERRED_NAME)))
  private String placePreferredPostProcessorWithoutNormdatumWithDefaultValue;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:msIdentifier/tei:settlement", attribute = "key", processingUnits = @ProcessingUnit(processorClass = PlacePostProcessor.class)))
  private String placePostProcessorWithAuthorityFileReference;

  @XMLSource(selectors = @Selector(xPath = "//tei:titleStmt/tei:respStmt/tei:persName[2]", attribute = "key", additionalValue = "text()", processingUnits = @ProcessingUnit(processorClass = PersonPostProcessor.class)))
  private String personPostProcessorWithoutAuthorityFileReferenceAndDefaultValue;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:head/tei:index[@indexName='norm_origPlace']/tei:term[@type='origPlace']", attribute = "key", defaultValue = "text()"))
  private String[] origPlaceKeyWithDefaultValue;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:head/tei:index[@indexName='norm_origPlace']/tei:term[@type='origPlace']", attribute = "key", additionalValue = "text()"))
  private String[] origPlaceKeyWithAdditionalValue;

  @XMLSource(selectors = @Selector(xPath = "//tei:msDesc/tei:head/tei:index[@indexName='norm_origPlace']/tei:term[@type='origPlace']", attribute = "key_invalid", defaultValue = "text()"))
  private String[] emptyOrigPlaceWithDefaultValue;
}
