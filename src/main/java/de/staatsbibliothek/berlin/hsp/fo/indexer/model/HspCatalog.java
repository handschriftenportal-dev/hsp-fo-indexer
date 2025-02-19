package de.staatsbibliothek.berlin.hsp.fo.indexer.model;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.ProcessingUnit;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.Selector;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.XMLSource;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ResultMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes.PersonPostProcessor;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes.YearPostProcessor;
import lombok.Data;
import org.apache.solr.client.solrj.beans.Field;

/**
 * Class for mapping TEI
 * Do not use copyField rules, even if the content of two properties will be the same,
 * since this will unnecessarily copy properties content for other entities
 */
@Data
public class HspCatalog implements HspBase {

  @Field("author-display")
  @XMLSource(selectors =
    @Selector(
        xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:respStmt/tei:persName[@role='author']",
        attribute = "key",
        processingUnits = @ProcessingUnit(processorClass = PersonPostProcessor.class, mapper = ResultMapper.PREFERRED_NAME),
        defaultValue = "text()",
        isMultiValue = true
    )
  )
  private String[] authorDisplay;

  @Field("catalog-author-facet")
  @XMLSource(selectors =
    @Selector(
        xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:respStmt/tei:persName[@role='author']",
        attribute = "key",
        processingUnits = @ProcessingUnit(processorClass = PersonPostProcessor.class, mapper = ResultMapper.PREFERRED_NAME),
        defaultValue = "text()",
        isMultiValue = true
    )
  )
  private String[] authorFacet;

  @Field("author-search")
  @XMLSource(selectors =
    @Selector(
        xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:respStmt/tei:persName[@role='author']",
        attribute = "key",
        processingUnits = @ProcessingUnit(processorClass = PersonPostProcessor.class, mapper = ResultMapper.ALL),
        additionalValue = "text()",
        isMultiValue = true)
  )
  private String[] authorSearch;

  @Field("editor-display")
  @XMLSource(selectors =
    @Selector(
        xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:editionStmt/tei:respStmt/tei:persName[@role='editor']",
        attribute = "key",
        processingUnits = @ProcessingUnit(processorClass = PersonPostProcessor.class, mapper = ResultMapper.PREFERRED_NAME),
        defaultValue = "text()",
        isMultiValue = true
    )
  )
  private String[] editorDisplay;

  @Field("editor-search")
  @XMLSource(selectors =
    @Selector(
        xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:editionStmt/tei:respStmt/tei:persName[@role='editor']",
        attribute = "key",
        processingUnits = @ProcessingUnit(processorClass = PersonPostProcessor.class, mapper = ResultMapper.ALL),
        additionalValue = "text()",
        isMultiValue = true
    )
  )
  private String[] editorSearch;

  @Field("fulltext-search")
  @XMLSource(selectors =
    @Selector(
        xPath = "/tei:TEI/tei:text/tei:body//text()", isMultiValue = true
    )
  )
  private String fullTextSearch;

  @Field("id")
  @XMLSource(selectors =
    @Selector(
        xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:idno[@type='hsp']"
    )
  )
  private String id;

  @Field("manifest-uri-display")
  @XMLSource(selectors =
    @Selector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:ptr[@type='hsp']/@target")
  )
  private String manifestURIDisplay;

  @Field("publisher-display")
  @XMLSource(selectors =
  @Selector(
      xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:publisher",
      attribute = "key",
      processingUnits = @ProcessingUnit(processorClass = PersonPostProcessor.class, mapper = ResultMapper.PREFERRED_NAME),
      defaultValue = "text()",
      isMultiValue = true)
  )
  private String[] publisherDisplay;

  @Field("catalog-publisher-facet")
  @XMLSource(selectors =
    @Selector(
        xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:publisher",
        attribute = "key",
        processingUnits = @ProcessingUnit(processorClass = PersonPostProcessor.class, mapper = ResultMapper.PREFERRED_NAME),
        defaultValue = "text()",
        isMultiValue = true
    )
  )
  private String[] publisherFacet;

  @Field("publisher-search")
  @XMLSource(selectors =
    @Selector(
        xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:publisher",
        attribute = "key",
        processingUnits = @ProcessingUnit(processorClass = PersonPostProcessor.class, mapper = ResultMapper.ALL),
        additionalValue = "text()",
        isMultiValue = true
    )
  )
  private String[] publisherSearch;

  @Field("publish-year-search")
  @XMLSource(selectors =
    @Selector(
        xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:date",
        processingUnits = @ProcessingUnit(processorClass = YearPostProcessor.class)
    )
  )
  private Integer publishYearSearch;

  @Field("publish-year-sort")
  @XMLSource(selectors =
  @Selector(
          xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:date",
          processingUnits = @ProcessingUnit(processorClass = YearPostProcessor.class)
  )
  )
  private Integer publishYearSort;

  @Field("catalog-publish-year-facet")
  @XMLSource(selectors =
  @Selector(
      xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:date",
      processingUnits = @ProcessingUnit(processorClass = YearPostProcessor.class)
  )
  )
  private Integer[] publishYearFacet;


  @Field("catalog-repository-facet")
  @XMLSource(selectors =
  @Selector(
      xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:repository"
  )
  )
  private String repositoryFacet;

  @XMLSource(selectors =
  @Selector(
      xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/tei:msIdentifier/tei:settlement"
  )
  )
  private String settlementFacet;

  @Field(value = "tei-document-display")
  private String teiDocumentDisplay;

  @Field("title-search")
  @XMLSource(selectors =
    @Selector(
        xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title"
    )
  )
  private String titleSearch;

  @Field("type-search")
/*  @XMLSource(selectors =
    @FieldSelector(xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:msDesc/@type")
  )*/
  public String typeSearch = "hsp:catalog";

  @Field("thumbnail-uri-display")
  @XMLSource(selectors =
    @Selector(
        xPath = "/tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:ptr[@type='thumbnail']/@target"
    )
  )
  public String thumbnailUrlDisplay;
}
