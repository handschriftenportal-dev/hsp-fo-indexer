package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten.NormdatenService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.XMLSource;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.XMLSources;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.entity.MappingTestModel;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.xml.XPathEvaluator;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspObject;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.ActivityMessageHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.AnnotationHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.fixture.Fixtures;
import de.staatsbibliothek.berlin.hsp.fo.indexer.type.HspObjectType;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.mapper.ObjectMapperFactory;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.model.ActivityStreamMessage;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.tree.DefaultElement;
import org.exparity.hamcrest.date.DateMatchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.AnnotationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.fixture.FileFixture.dataFromResourceFilename;
import static de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.fixture.NormdatenFixture.runWithNormdatenService;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.ArrayMatching.arrayContainingInAnyOrder;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

@ExtendWith(SpringExtension.class)
public class SolrMapperTest {
  static SolrMapper solrMapper;
  static ObjectMapper objectMapper;
  final Resource kodRes;
  final Resource descRes;
  final Resource descRes2;
  final byte[] kodContent;
  final byte[] descContent;
  final byte[] descContent02;
  final ActivityStreamMessage asm_loremIpsum;

  public SolrMapperTest(@Autowired final ResourceLoader rLoader) throws Exception {
    kodRes = rLoader.getResource("fixtures/loremIpsum_kod.xml");
    descRes = rLoader.getResource("fixtures/loremIpsum_beschreibung.xml");
    descRes2 = rLoader.getResource("fixtures/loremIpsum_beschreibung_modifiziert.xml");

    kodContent = kodRes.getInputStream().readAllBytes();
    descContent = descRes.getInputStream().readAllBytes();
    descContent02 = descRes2.getInputStream().readAllBytes();

    asm_loremIpsum = ActivityMessageHelper.getActivityStreamMessageFromXML(
            ActivityMessageHelper.getASDKOD(kodRes),
            ActivityMessageHelper.getASDBeschreibung(descRes),
            ActivityMessageHelper.getASDBeschreibung(descRes2)
    );
  }

  private static HspObject getHspObject() {
    final HspObject hspObject = new HspObject();
    hspObject.setTypeSearch(HspObjectType.OBJECT.getValue()[0]);
    return hspObject;
  }

  private static HspDescription getHspDescription() {
    final HspDescription hspDescription = new HspDescription();
    hspDescription.setTypeSearch(HspObjectType.DESCRIPTION.getValue()[0]);
    return hspDescription;
  }

  private byte[] getKODContent() {
    return kodContent;
  }

  private List<byte[]> getDescriptionContents() {
    return List.of(descContent, descContent02);
  }

  @BeforeAll
  public static void setup() {
    objectMapper = ObjectMapperFactory.getObjectMapper();
    solrMapper = new SolrMapper();
  }

  private HspDescription mapDescription(final String filename, final HspObject hspObject) throws IOException, DocumentException {
    return solrMapper.mapHspDescription(dataFromResourceFilename(filename), hspObject, "yes", "yes");
  }

  private HspObject mapHspObject(final String filename) throws IOException, DocumentException {
    return solrMapper.mapHspObject(dataFromResourceFilename(filename));
  }

  @AfterEach
  void tearDown() {
    solrMapper.setNormDatenService(null);
  }

  @Test
  void whenAdditionalValueIsSet_thenAdditionalValueIsUsed() throws Exception {
    final byte[] doc = dataFromResourceFilename("fixtures/14025_description_with_gnd_references.xml");

    List<Field> filteredFields = AnnotationUtils.findAnnotatedFields(MappingTestModel.class, XMLSource.class, (Field f) -> f.getName()
            .equals("origPlaceKeyWithAdditionalValue"));
    runWithNormdatenService(() -> {
      final List<String> result = solrMapper.getFieldValue(new XPathEvaluator(doc), filteredFields.get(0));
      assertThat(result, hasSize(2));
      assertThat(result, hasItems("Großbritannien, Nordharz", "a9862391-674b-4383-9721-a6eb63ae6cb4"));
    }, solrMapper);
  }

  @Test
  void whenXpathHits_thenDefaultValueIsNotPartOfResult() throws Exception {
    final byte[] doc = dataFromResourceFilename("fixtures/14025_description_with_gnd_references.xml");

    List<Field> filteredFields = AnnotationUtils.findAnnotatedFields(MappingTestModel.class, XMLSource.class, (Field f) -> f.getName()
            .equals("origPlaceKeyWithDefaultValue"));

    runWithNormdatenService(() -> {
      final List<String> result = solrMapper.getFieldValue(new XPathEvaluator(doc), filteredFields.get(0));
      assertThat(result, hasSize(1));
      assertThat(result, hasItem("a9862391-674b-4383-9721-a6eb63ae6cb4"));
    }, solrMapper);
  }

  @Test
  void whenXpathMisses_thenDefaultValueIsUsed() throws Exception {
    final byte[] doc = dataFromResourceFilename("fixtures/14025_description_with_gnd_references.xml");

    List<Field> filteredFields = AnnotationUtils.findAnnotatedFields(MappingTestModel.class, XMLSource.class, (Field f) -> f.getName()
            .equals("emptyOrigPlaceWithDefaultValue"));
    runWithNormdatenService(() -> {
      final List<String> result = solrMapper.getFieldValue(new XPathEvaluator(doc), filteredFields.get(0));
      assertThat(result, hasSize(1));
      assertThat(result, hasItem("Großbritannien, Nordharz"));
    }, solrMapper);
  }

  @Nested
  @DisplayName("xPath Expression Evaluation")
  class xPathEvaluation {

    @Test
    @DisplayName("Wrong xPath")
    void testGatherMultipleNodesByXPathExpression_NoElements() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
      final String xPathExpression = "//tei:teiHeader2";

      final List<Node> nodes = new XPathEvaluator(doc).gatherNodes(xPathExpression);

      assertThat(nodes, hasSize(0));
    }

    @Test
    @DisplayName("Multiple text nodes")
    void testGatherMultipleNodesByXPathExpression_SimpleText() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
      final String xPathExpression = "//tei:teiHeader/tei:fileDesc/tei:editionStmt/tei:respStmt/tei:name";

      final List<Node> nodes = new XPathEvaluator(doc).gatherNodes(xPathExpression);

      assertThat(nodes, hasSize(2));
      assertThat(nodes.get(0).getText(), equalTo("Bertram Lesser"));
      assertThat(nodes.get(1).getText(), equalTo("Herzog August Bibliothek"));
    }

    @Test
    @DisplayName("Text node with condition")
    void testGatherNodeByXPathExpression_AttributeCondition() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
      final String xPathExpression = "//tei:teiHeader/tei:fileDesc/tei:editionStmt/tei:respStmt/tei:name[@type=\"org\"]";

      final Optional<Node> node = new XPathEvaluator(doc).gatherNode(xPathExpression);

      assertThat(node, isPresent());
      assertThat(node.get().getText(), equalTo("Herzog August Bibliothek"));
    }

    @Test
    @DisplayName("Text node")
    void testGatherNodeByXPathExpression_simpleText() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
      final String xPathExpression = "//tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title";

      final Optional<Node> node = new XPathEvaluator(doc).gatherNode(xPathExpression);

      assertThat(node, isPresent());
      assertThat(node.get().getText(), equalTo("Beschreibung von Cod. Guelf. 277 Helmst. (Die mittelalterlichen Helmstedter Handschriften der Herzog August Bibliothek. Teil 2: Cod. Guelf. 277 bis 370 Helmst. und Helmstedter Fragmente. Beschrieben von Bertram Lesser. Wiesbaden: Harrassowitz, (im Erscheinen).)"));
    }
  }

  @Nested
  @DisplayName("Annotation based mapping")
  class XMLSourceEvaluation {

    @Test
    @DisplayName("Multiple XMLSources with attribute xPath, multivalued")
    void testGetAttributeValue_MultiNode_MultiValued() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
      List<Field> filteredFields = AnnotationUtils.findAnnotatedFields(MappingTestModel.class, XMLSources.class, (Field f) -> f.getName()
              .equals("testAttr_multiNode_multiSelector_multiValue"));
      assertThat(filteredFields.size(), equalTo(1));

      final List<String> values = solrMapper.getFieldValue(new XPathEvaluator(doc), filteredFields.get(0));

      assertThat(values, notNullValue());
      assertThat(values.size(), equalTo(2));
      assertThat(values, hasItem("29,5"));
      assertThat(values, hasItem("14"));
    }

    @Test
    @DisplayName("Multiple XMLSources with attribute xPath")
    void testGetAttributeValue_MultiNode_SingleValued() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
      final List<Field> filteredFields = AnnotationUtils.findAnnotatedFields(MappingTestModel.class, XMLSources.class, (Field f) -> f.getName()
              .equals("testAttr_multiNode_singleSelector"));
      assertThat(filteredFields.size(), equalTo(1));

      final List<String> values = solrMapper.getFieldValue(new XPathEvaluator(doc), filteredFields.get(0));

      assertThat(values, notNullValue());
      assertThat(values.size(), equalTo(1));
      assertThat(values.get(0), equalTo("14"));
    }

    @Test
    @DisplayName("Multiple XMLSources with attribute xPath, prioritized")
    void testGetAttributeValue_MultiNode_SingleValued_Prioritized() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
      List<Field> filteredFields = AnnotationUtils.findAnnotatedFields(MappingTestModel.class, XMLSources.class, (Field f) -> f.getName()
              .equals("testAttr_multiNode_singleSelector_prioritized"));
      assertThat(filteredFields.size(), equalTo(1));

      final List<String> values = solrMapper.getFieldValue(new XPathEvaluator(doc), filteredFields.get(0));

      assertThat(values, notNullValue());
      assertThat(values.size(), equalTo(1));
      assertThat(values.get(0), equalTo("29,5"));
    }

    @Test
    @DisplayName("Multiple XMLSources with attribute xPath, with first XMLSource empty")
    void testGetAttributeValue_MultiNode_SingleValued_firstSelectorIsEmpty() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
      final List<Field> filteredFields = AnnotationUtils.findAnnotatedFields(MappingTestModel.class, XMLSources.class, (Field f) -> f.getName()
              .equals("testAttr_multiNode_singleSelector_firstEmpty"));
      assertThat(filteredFields.size(), equalTo(1));

      final List<String> values = solrMapper.getFieldValue(new XPathEvaluator(doc), filteredFields.get(0));

      assertThat(values, notNullValue());
      assertThat(values.size(), equalTo(1));
      assertThat(values.get(0), equalTo("29,5"));
    }

    @Test
    @DisplayName("XMLSource with attribute xPath and multiValue")
    void testGetAttributeValue_MultiValued() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
      final List<Field> filteredFields = AnnotationUtils.findAnnotatedFields(MappingTestModel.class, XMLSource.class, (Field f) -> f.getName()
              .equals("testAttrSingleSelectorMulti"));
      assertThat(filteredFields.size(), equalTo(1));
      Optional<XMLSource> optAttribute = AnnotationUtils.findAnnotation(filteredFields.get(0), XMLSource.class);
      assertThat(optAttribute, isPresent());

      final List<String> values = solrMapper.getAttributeFieldValue(new XPathEvaluator(doc), optAttribute.get());

      assertThat(values, notNullValue());
      assertThat(values.size(), equalTo(2));
      assertThat(values.get(0), equalTo("com"));
      assertThat(values.get(1), equalTo("org"));
    }

    @Test
    @DisplayName("XMLSource with attribute xPath")
    void testGetAttributeValue_SingleValued() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
      final List<Field> filteredFields = AnnotationUtils.findAnnotatedFields(MappingTestModel.class, XMLSource.class, (Field f) -> f.getName()
              .equals("testAttrSingleSelector"));
      assertThat(filteredFields.size(), equalTo(1));
      Optional<XMLSource> optAttribute = AnnotationUtils.findAnnotation(filteredFields.get(0), XMLSource.class);
      assertThat(optAttribute, isPresent());

      final List<String> values = solrMapper.getAttributeFieldValue(new XPathEvaluator(doc), optAttribute.get());

      assertThat(values, notNullValue());
      assertThat(values.size(), equalTo(1));
      assertThat(values.get(0), equalTo("person"));
    }

    @Test
    @DisplayName("XMLSource with multiple attribute xPath")
    void testGetAttributeValue_SingleValued_MultiXPathExpressions() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
      final List<Field> filteredFields = AnnotationUtils.findAnnotatedFields(MappingTestModel.class, XMLSource.class, (Field f) -> f.getName()
              .equals("testAttrMultiSelector"));
      assertThat(filteredFields.size(), equalTo(1));
      Optional<XMLSource> optAttribute = AnnotationUtils.findAnnotation(filteredFields.get(0), XMLSource.class);
      assertThat(optAttribute, isPresent());

      final List<String> values = solrMapper.getAttributeFieldValue(new XPathEvaluator(doc), optAttribute.get());

      assertThat(values, notNullValue());
      assertThat(values.size(), equalTo(3));
      assertThat(values.get(0), equalTo("person"));
      assertThat(values.get(1), equalTo("com"));
      assertThat(values.get(2), equalTo("org"));
    }

    @Test
    @DisplayName("XMLSource with text node xPath and multiValue")
    void testGetTextFieldValue_MultiValued() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
      final String xPathExpression = "//tei:teiHeader/tei:fileDesc/tei:editionStmt/tei:respStmt/tei:name";
      final XMLSource attrNode = AnnotationHelper.getXMLSource(xPathExpression, true, 0);

      final List<String> res = XPathEvaluator.getTextFieldValue((List<DefaultElement>) (List<?>) new XPathEvaluator(doc).getElements(attrNode.selectors()[0].isMultiValue(), attrNode.selectors()[0].xPath()));

      assertThat(res, notNullValue());
      assertThat(res.size(), equalTo(2));
      assertThat(res.get(0), equalTo("Bertram Lesser"));
      assertThat(res.get(1), equalTo("Herzog August Bibliothek"));
    }

    @Test
    @DisplayName("XMLSource with text node xPath")
    void testGetTextFieldValue_SingleValued() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
      final String xPathExpression = "//tei:teiHeader/tei:fileDesc/tei:editionStmt/tei:respStmt/tei:name";
      final XMLSource attrNode = AnnotationHelper.getXMLSource(xPathExpression, false, 0);

      final List<String> res = XPathEvaluator.getTextFieldValue((List<DefaultElement>) (List<?>) new XPathEvaluator(doc).getElements(attrNode.selectors()[0].isMultiValue(), attrNode.selectors()[0].xPath()));

      assertThat(res, notNullValue());
      assertThat(res.size(), equalTo(1));
      assertThat(res.get(0), equalTo("Bertram Lesser"));
    }

    @Test
    void whenHspObjectIsMapped_thenIdnoSortKeyIsMappedCorrectly() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getIdnoSortKey(), is("02f-0001-0005-0001"));
    }
  }

  @Nested
  class PostProcessor {
    @Test
    void whenMappingWithCorporatePreferredProcessorByKey_thenResultingValueContainsOnlyPreferredName() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/12505_tei_kod_with_normdaten.xml");
      List<Field> filteredFields = AnnotationUtils.findAnnotatedFields(MappingTestModel.class, XMLSource.class, (Field f) -> f.getName()
              .equals("corporatePreferredPostProcessorWithNormdatum"));

      runWithNormdatenService(() -> {
        final List<String> result = solrMapper.getFieldValue(new XPathEvaluator(doc), filteredFields.get(0));
        assertThat(result, hasSize(1));
        assertThat(result.get(0), is("test preferred name"));
      }, solrMapper);
    }

    @Test
    void whenMappingWithCorporatePreferredProcessorWithoutNormdatumButDefaultValue_thenResultIsDefaultValue() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/14025_description_with_gnd_references.xml");

      List<Field> filteredFields = AnnotationUtils.findAnnotatedFields(MappingTestModel.class, XMLSource.class, (Field f) -> f.getName()
              .equals("corporatePreferredPostProcessorWithoutNormdatumWithDefault"));

      runWithNormdatenService(() -> {
        final List<String> result = solrMapper.getFieldValue(new XPathEvaluator(doc), filteredFields.get(0));
        assertThat(result, hasSize(1));
        assertThat(result, hasItem("Staatsbibliothek zu Berlin"));
      }, solrMapper);
    }

    @Test
    void whenMappingWithCorporateProcessor_thenResultingValueContainsAllTheNormdatumInformation() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/12505_tei_kod_with_normdaten.xml");
      List<Field> filteredFields = AnnotationUtils.findAnnotatedFields(MappingTestModel.class, XMLSource.class, (Field f) -> f.getName()
              .equals("corporatePostProcessorWithNormdatum"));

      runWithNormdatenService(() -> {
        final List<String> result = solrMapper.getFieldValue(new XPathEvaluator(doc), filteredFields.get(0));
        assertThat(result, hasSize(7));
        assertThat(result, hasItem("test id"));
        assertThat(result, hasItem("gnd test id"));
        assertThat(result, hasItem("test name"));
        assertThat(result, hasItem("test preferred name"));
        assertThat(result, hasItem("test variant name 01"));
        assertThat(result, hasItem("test variant name 02"));
        assertThat(result, hasItem("test text"));
      }, solrMapper);
    }

    @Test
    void whenMappingWithPlaceProcessor_thenResultingValueContainsAllValues() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/14025_description_with_gnd_references.xml");
      List<Field> filteredFields = AnnotationUtils.findAnnotatedFields(MappingTestModel.class, XMLSource.class, (Field f) -> f.getName()
              .equals("placePostProcessorWithNormDatum"));
      runWithNormdatenService(() -> {
        final List<String> result = solrMapper.getFieldValue(new XPathEvaluator(doc), filteredFields.get(0));
        assertThat(result, containsInAnyOrder("5036103-X", "NORM-774909e2-f687-30cb-a5c4-ddc95806d6be", "DE-1", "soz_30002258", "Dt. SB", "Staatsbibliothek zu Berlin - Preußischer Kulturbesitz", "Gosudarstvennaja Biblioteka v Berline - Prusskoe Kulʹturnoe Nasledie", "SBPK", "Bibliotheca Regia Berolinensis", "Staatsbibliothek zu Berlin"));
      }, solrMapper);
    }

    @Test
    void whenMappingWithPlaceProcessor_thenResultingValueContainsPlacesInformation() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/loremIpsum_beschreibung.xml");

      List<Field> filteredFields = AnnotationUtils.findAnnotatedFields(MappingTestModel.class, XMLSource.class, (Field f) -> f.getName()
              .equals("placePostProcessorWithNormDatum"));

      NormdatenService normService = new NormdatenService("localhost:56789");
      normService.setRestTemplate(new RestTemplate());
      solrMapper.setNormDatenService(normService);

      runWithNormdatenService(() -> {
        final List<String> result = solrMapper.getFieldValue(new XPathEvaluator(doc), filteredFields.get(0));
        assertThat(result, hasItems("4030481-4", "NORM-1a75b353-14be-3c19-b22c-5c5334d78c8f", "Kiel"));
      }, solrMapper);
    }

    @Test
    void whenMappingWithPreferredPlaceProcessorWithoutNormdatum_thenResultIsDefaultValue() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/14025_description_with_gnd_references.xml");

      List<Field> filteredFields = AnnotationUtils.findAnnotatedFields(MappingTestModel.class, XMLSource.class, (Field f) -> f.getName()
              .equals("corporatePreferredPostProcessorWithoutNormdatumWithDefault"));

      runWithNormdatenService(() -> {
        final List<String> result = solrMapper.getFieldValue(new XPathEvaluator(doc), filteredFields.get(0));
        assertThat(result, hasSize(1));
        assertThat(result, hasItem("Staatsbibliothek zu Berlin"));
      }, solrMapper);
    }

    @Test
    void whenMappingWithPreferredPlaceProcessor_thenResultingValueContainsOnlyPlacesPreferredName() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/loremIpsum_beschreibung.xml");

      List<Field> filteredFields = AnnotationUtils.findAnnotatedFields(MappingTestModel.class, XMLSource.class, (Field f) -> f.getName()
              .equals("placePreferredPostProcessorWithNormDatum"));
      runWithNormdatenService(() -> {
        final List<String> result = solrMapper.getFieldValue(new XPathEvaluator(doc), filteredFields.get(0));
        assertThat(result, hasItems("Kiel"));
      }, solrMapper);
    }

    @Test
    void whenMappingWithPreferredPlaceProcessor_thenResultingValueContainsPreferredNames() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/14025_description_with_gnd_references.xml");

      List<Field> filteredFields = AnnotationUtils.findAnnotatedFields(MappingTestModel.class, XMLSource.class, (Field f) -> f.getName()
              .equals("placePreferredPostProcessorWithNormDatum"));

      runWithNormdatenService(() -> {
        final List<String> result = solrMapper.getFieldValue(new XPathEvaluator(doc), filteredFields.get(0));
        assertThat(result, hasSize(1));
        assertThat(result, containsInAnyOrder("Staatsbibliothek zu Berlin"));
      }, solrMapper);
    }

    @Test
    void whenMappingWithPreferredPlaceProcessorAndMissingNormdatum_thenResultingValueIsEmpty() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/14025_description_with_gnd_references.xml");

      List<Field> filteredFields = AnnotationUtils.findAnnotatedFields(MappingTestModel.class, XMLSource.class, (Field f) -> f.getName()
              .equals("placePreferredPostProcessorWithoutNormDatum"));

      runWithNormdatenService(() -> {
        final List<String> result = solrMapper.getFieldValue(new XPathEvaluator(doc), filteredFields.get(0));
        assertThat(result, hasSize(0));
      }, solrMapper);
    }
  }

  @Nested
  @DisplayName("Retro Description Mapping")
  class RetroDescription {
    @Test
    void whenRetroDescriptionIsMapped_thenAllAttributesAreMappedCorrectly() throws Exception {
      final byte[] tei = dataFromResourceFilename("fixtures/14227_description_retro.xml");

      final HspDescription hspDescription = solrMapper.mapHspDescription(tei, new HspObject(), "FALSE", "FALSE");

      assertThat(hspDescription.getCatalogIIIFManifestRangeUrlDisplay(), is("https://iiif.ub.uni-leipzig.de/0000034537/range/LOG_0014"));
      assertThat(hspDescription.getCatalogIIIFManifestUrlDisplay(), is("https://iiif.ub.uni-leipzig.de/0000034537/manifest.json"));
      assertThat(hspDescription.getCatalogIdDisplay(), is("HSP-a8abb4bb-284b-3b27-aa7c-b790dc20f80b"));

      assertThat(hspDescription.getFulltextSearch(), containsString(Fixtures.RETRO_DESCRIPTION_MSPART_OTHER));
    }
  }

  @Nested
  @DisplayName("Description Mapping")
  class Description {
    @Test
    @DisplayName("accompanying-material-search field")
    void whenDescriptionIsMapped_thenAccompanyingMaterialSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getAccompanyingMaterialSearch(), is(Fixtures.LOREM_IPSUM_DESCRIPTION_ACCOMPANYING_MATERIAL));
    }

    @Test
    @DisplayName("binding-orig-place-search field")
    void whenDescriptionIsMapped_thenBindingOrigPlaceSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getBindingOrigPlaceSearch(), arrayContainingInAnyOrder("Konstanz, Bodenseeraum", "4007405-5", "4264875-5"));
    }

    @Test
    @DisplayName("binding-search field")
    void whenDescriptionIsMapped_thenBindingSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getBindingSearch(), is(Fixtures.LOREM_IPSUM_DESCRIPTION_BINDING));
    }

    @Test
    @DisplayName("booklet-search field")
    void whenDescriptionIsMapped_thenBookletSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getBookletSearch(), is(Fixtures.LOREM_IPSUM_DESCRIPTION_BOOKLET));
    }

    @Test
    @DisplayName("collection-search field")
    void whenDescriptionIsMapped_thenCollectionSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getCollectionSearch(), is("Aktuelle Sammlung"));
    }

    @Test
    @DisplayName("decoration-search field")
    void whenDescriptionIsMapped_thenDecorationSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getDecorationSearch(), is(Fixtures.LOREM_IPSUM_DESCRIPTION_DECORATION));
    }

    @Test
    @DisplayName("depth-facet field")
    void whenDescriptionIsMapped_thenDepthFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getDepthFacet(), arrayContainingInAnyOrder(2F));
    }

    @Test
    @DisplayName("dimension-display field")
    void whenDescriptionIsMapped_thenDimensionsDisplayIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getDimensionsDisplay(), arrayContainingInAnyOrder("16 × 12 (Teil I)", "12,5 × 15,5 (Teil II)"));
    }

    @Test
    @DisplayName("explicit-search field")
    void whenDescriptionIsMapped_thenExplicitSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getExplicitSearch(), arrayContainingInAnyOrder("sed libera nos a malo.", "et accusam et justo duo"));
    }

    @Test
    @DisplayName("format-facet field")
    void whenDescriptionIsMapped_thenFormatFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getFormatFacet(), arrayContaining("folio", "oblong"));
    }

    @Test
    @DisplayName("format-search field")
    void whenDescriptionIsMapped_thenFormatSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getFormatSearch(), arrayContainingInAnyOrder("folio", "oblong"));
    }

    @Test
    @DisplayName("format-type-display field")
    void whenDescriptionIsMapped_thenFormatTypeDisplayIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getFormatTypeDisplay(), arrayContainingInAnyOrder("deduced", "factual"));
    }

    @Test
    @DisplayName("format-type-facet field")
    void whenDescriptionIsMapped_thenFormatTypeFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getFormatTypeFacet(), arrayContainingInAnyOrder("deduced", "factual"));
    }

    @Test
    @DisplayName("fragment-search field")
    void whenDescriptionIsMapped_thenFragmentSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getFragmentSearch(), is(Fixtures.LOREM_IPSUM_DESCRIPTION_FRAGMENT));
    }

    @Test
    @DisplayName("fulltext-search field")
    void whenDescriptionIsMapped_thenFulltextSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getFulltextSearch(), not(containsString("Mischung aus Papier und Leinen")));
    }

    @Test
    @DisplayName("has-notation-display field")
    void whenDescriptionIsMapped_thenHasNotationDisplayIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getHasNotationDisplay(), is("no"));
    }

    @Test
    @DisplayName("has-notation-facet field")
    void whenDescriptionIsMapped_thenHasNotationFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getHasNotationFacet(), arrayContainingInAnyOrder("no"));
    }

    @Test
    @DisplayName("height-facet field")
    void whenDescriptionIsMapped_thenHeightFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getHeightFacet(), arrayContainingInAnyOrder(16F, 12.5F));
    }

    @Test
    @DisplayName("history-search field")
    void whenDescriptionIsMapped_thenHistorySearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getHistorySearch(), is(Fixtures.LOREM_IPSUM_DESCRIPTION_HISTORY));
    }

    @Test
    @DisplayName("id field")
    void whenDescriptionIsMapped_thenIdIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getId(), is("__UUID__"));
    }

    @Test
    @DisplayName("idno-alternative-search field")
    void whenDescriptionIsMapped_thenIdnoAltSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getIdnoAltSearch(), arrayContainingInAnyOrder("HSP-3d18a39c-1429-341e-b397-ca2bb8f9cdfb", "obj_123456", "dolor sit amet", "St. Emm 57"));
    }

    @Test
    @DisplayName("idno-search field")
    void whenDescriptionIsMapped_thenIdnoSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getIdnoSearch(), is("Cod. ms. Bord. 1"));
    }

    @Test
    @DisplayName("idno-sort field")
    void whenDescriptionIsMapped_thenIdnoSortKeyIsMappedCorrectly() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getIdnoSortKey(), is("02f-0001-0005-0001"));
    }

    @Test
    @DisplayName("illuminated-display field")
    void whenDescriptionIsMapped_thenIlluminateDisplayIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getIlluminatedDisplay(), is("yes"));
    }

    @Test
    @DisplayName("illuminated-facet field")
    void whenDescriptionIsMapped_thenIlluminatedFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getIlluminatedFacet(), arrayContainingInAnyOrder("yes"));
    }

    @Test
    @DisplayName("incipit-search field")
    void whenDescriptionIsMapped_thenIncipitSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getIncipitSearch(), arrayContainingInAnyOrder("Lorem ipsum dolor sit amet", "Abbatis Siculi repertorium n vii", "et dolore magna"));
    }

    @Test
    @DisplayName("item-iconography-search field")
    void whenDescriptionIsMapped_thenItemIconographySearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getItemIconographySearch(), is(Fixtures.LOREM_IPSUM_DESCRIPTION_ITEM_ICONOGRAPHY));
    }

    @Test
    @DisplayName("item-music-search field")
    void whenDescriptionIsMapped_thenItemMusicSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getItemMusicSearch(), is(Fixtures.LOREM_IPSUM_DESCRIPTION_ITEM_MUSIC));
    }

    @Test
    @DisplayName("item-text-search field")
    void whenDescriptionIsMapped_thenItemTextSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getItemTextSearch(), is(Fixtures.LOREM_IPSUM_DESCRIPTION_ITEM_TEXT));
    }

    @Test
    @DisplayName("language-display field")
    void whenDescriptionIsMapped_thenLanguageDisplayIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getLanguageDisplay(), arrayContainingInAnyOrder("de", "la"));
    }

    @Test
    @DisplayName("language-facet field")
    void whenDescriptionIsMapped_thenLanguageFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getLanguageFacet(), arrayContainingInAnyOrder("de", "la"));
    }

    @Test
    @DisplayName("language-search field")
    void whenDescriptionIsMapped_thenLanguageSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getLanguageSearch(), arrayContainingInAnyOrder("de", "la", "la bg", "Texte in Deutsch, Latein und Bulgarisch", "lateinisch, deutsch"));
    }

    @Test
    @DisplayName("last-modified-display field")
    void whenDescriptionIsMapped_thenLastModifiedIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getLastModifiedDisplay(), DateMatchers.isDay(2020, Month.JANUARY, 2));
    }

    @Test
    @DisplayName("leaves-count-display field")
    void whenDescriptionIsMapped_thenLeavesCountDisplayIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getLeavesCountDisplay(), is("103 Bl., aus zwei Teilen zusammengesetzt"));
    }

    @Test
    @DisplayName("leaves-count-facet field")
    void whenDescriptionIsMapped_thenLeavesCountFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getLeavesCountFacet(), arrayContaining(103));
    }

    @Test
    @DisplayName("material-display field")
    void whenDescriptionIsMapped_thenMaterialDisplayIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getMaterialDisplay(), is("Mischung aus Papier und Leinen"));
    }

    @Test
    @DisplayName("material-facet field")
    void whenDescriptionIsMapped_thenMaterialFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getMaterialFacet(), arrayContainingInAnyOrder("linen", "paper"));
    }

    @Test
    @DisplayName("material-search field")
    void whenDescriptionIsMapped_thenMaterialSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getMaterialSearch(), arrayContaining("Mischung aus Papier und Leinen", "linen", "paper"));
    }

    @Test
    @DisplayName("object-type-facet field")
    void whenDescriptionIsMapped_thenObjectTypeFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getObjectTypeFacet(), arrayContaining("codex"));
    }

    @Test
    @DisplayName("object-type-search field")
    void whenDescriptionIsMapped_thenObjectTypeSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getObjectTypeSearch(), is("codex"));
    }

    @Test
    @DisplayName("orig-date-from-facet field")
    void whenDescriptionIsMapped_thenOrigDateFromFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getOrigDateFromFacet(), arrayContainingInAnyOrder(1651, -14));
    }

    @Test
    @DisplayName("orig-date-from-search field")
    void whenDescriptionIsMapped_thenOrigDateFromSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getOrigDateFromSearch(), arrayContainingInAnyOrder(1651, -14));
    }

    @Test
    @DisplayName("orig-date-to-facet field")
    void whenDescriptionIsMapped_thenOrigDateToFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getOrigDateToFacet(), arrayContainingInAnyOrder(15, 1680));
    }

    @Test
    @DisplayName("orig-date-to-search field")
    void whenDescriptionIsMapped_thenOrigDateToSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getOrigDateToSearch(), arrayContainingInAnyOrder(15, 1680));
    }

    @Test
    @DisplayName("orig-date-type-facet field")
    void whenDescriptionIsMapped_thenOrigDateTypeFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getOrigDateTypeFacet(), arrayContainingInAnyOrder("datable", "dated"));
    }

    @Test
    @DisplayName("orig-date-when-facet field")
    void whenDescriptionIsMapped_thenOrigDateWhenFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getOrigDateWhenFacet(), arrayContainingInAnyOrder(1488));
    }

    @Test
    @DisplayName("orig-date-when-search field")
    void whenDescriptionIsMapped_thenOrigDateWhenSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getOrigDateWhenSearch(), arrayContainingInAnyOrder("1488", "um 1665 (Teil II)", "um 0 (Teil III)"));
    }

    @Test
    @DisplayName("orig-place-display field")
    void whenDescriptionIsMapped_thenOrigPlaceDisplayIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getOrigPlaceDisplay(), is("Staatsbibliothek zu Berlin, Berlin"));
    }

    @Test
    @DisplayName("orig-place-facet field")
    void whenDescriptionIsMapped_thenOrigPlaceFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getOrigPlaceFacet(), arrayContainingInAnyOrder("Staatsbibliothek zu Berlin", "Berlin"));
    }

    @Test
    @DisplayName("orig-place-search field")
    void whenDescriptionIsMapped_thenOrigPlaceSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getOrigPlaceSearch(), arrayContainingInAnyOrder("Staatsbibliothek zu Berlin, Berlin", "Staatsbibliothek zu Berlin", "Berlin"));
    }

    @Test
    @DisplayName("persistent-url-display field")
    void whenDescriptionIsMapped_thenPersistentURLDisplayIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersistentURLDisplay(), is("https://resolver.staatsbibliothek-berlin.de/__UUID__"));
    }

    @Test
    @DisplayName("person-author-search field")
    void whenDescriptionIsMapped_thenPersonAuthorIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersonAuthorSearch(), arrayContainingInAnyOrder("Corvinus, Johann August", "Katrin Sturm", "Konstantin Görlitz", "Martin Luther", "Augustus"));
    }

    @Test
    @DisplayName("person-bookbinder-search field")
    void whenDescriptionIsMapped_thenPersonBookbinderSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersonBookbinderSearch(), arrayContainingInAnyOrder("Tiberius"));
    }

    @Test
    @DisplayName("person-commissioned-by-search field")
    void whenDescriptionIsMapped_thenPersonCommissionedSearchByIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersonCommissionedBySearch(), arrayContainingInAnyOrder("Caligula"));
    }

    @Test
    @DisplayName("person-illuminator-search field")
    void whenDescriptionIsMapped_thenIlluminatorSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersonIlluminatorSearch(), arrayContainingInAnyOrder("Claudius"));
    }

    @Test
    @DisplayName("person-illuminator-search field")
    void whenDescriptionIsMapped_thenPersonMentionedSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersonMentionedInSearch(), arrayContainingInAnyOrder("Nero"));
    }

    @Test
    @DisplayName("person-mentioned-in-search field")
    void whenDescriptionIsMapped_thenPersonMentionedInSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersonMentionedInSearch(), arrayContainingInAnyOrder("Nero"));
    }

    @Test
    @DisplayName("person-other-search field")
    void whenDescriptionIsMapped_thenPersonOtherSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersonOtherSearch(), arrayContainingInAnyOrder("Galba"));
    }

    @Test
    @DisplayName("person-previous-owner-search field")
    void whenDescriptionIsMapped_thenPersonPreviousOwnerSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersonPreviousOwnerSearch(), arrayContainingInAnyOrder("Otho", "Vitellius"));
    }

    @Test
    @DisplayName("person-conservator-search field")
    void whenDescriptionIsMapped_thenPersonConservatorSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersonConservatorSearch(), arrayContainingInAnyOrder("Vespasian", "Katrin Sturm"));
    }

    @Test
    @DisplayName("person-scribe-search field")
    void whenDescriptionIsMapped_thenPersonScribeSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersonScribeSearch(), arrayContainingInAnyOrder("Titus"));
    }

    @Test
    @DisplayName("person-translator-search field")
    void whenDescriptionIsMapped_thenPersonTranslatorSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersonTranslatorSearch(), arrayContainingInAnyOrder("Domitian"));
    }

    @Test
    @DisplayName("physical-description-search field")
    void whenDescriptionIsMapped_thenPhysicalDescriptionSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPhysicalDescriptionSearch(), is(Fixtures.PHYSICAL_DESCRIPTION));
    }

    @Test
    @DisplayName("repository-display field")
    void whenDescriptionIsMapped_thenRepositoryDisplayIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getRepositoryDisplay(), is("Universitätsbibliothek"));
    }

    @Test
    @DisplayName("repository-facet field")
    void whenDescriptionIsMapped_thenRepositoryFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getRepositoryFacet(), is(nullValue()));
    }

    @Test
    @DisplayName("repository-search field")
    void whenDescriptionIsMapped_thenRepositorySearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getRepositorySearch(), arrayContainingInAnyOrder("Universitätsbibliothek"));
    }

    @Test
    @DisplayName("settlement-display field")
    void whenDescriptionIsMapped_thenSettlementDisplayIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getSettlementDisplay(), is("Kiel"));
    }

    @Test
    @DisplayName("settlement-facet field")
    void whenDescriptionIsMapped_thenSettlementFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getSettlementFacet(), is(nullValue()));
    }

    @Test
    @DisplayName("settlement-search field")
    void whenDescriptionIsMapped_thenSettlementSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getSettlementSearch(), arrayContainingInAnyOrder("Kiel"));
    }

    @Test
    @DisplayName("status-facet field")
    void whenDescriptionIsMapped_thenStatusFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getStatusFacet(), arrayContainingInAnyOrder("existent"));
    }

    @Test
    @DisplayName("status-search field")
    void whenDescriptionIsMapped_thenStatusSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getStatusSearch(), is("existent"));
    }

    @Test
    @DisplayName("tei-document-display field")
    void whenDescriptionIsMapped_thenTEIDocumentDisplayIsCorrect() throws Exception {
      final byte[] tei = dataFromResourceFilename("fixtures/loremIpsum_beschreibung.xml");
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getTeiDocumentDisplay(), is(new String(tei)));
    }

    @Test
    @DisplayName("title-search field")
    void whenDescriptionIsMapped_thenTitleSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getTitleSearch(), equalTo("Catalogi bibliothecae Bordesholmensis, Bordesholmer Handschriften"));
    }

    @Test
    @DisplayName("type-search field")
    void whenHspObjectIsMapped_thenTypeSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getTypeSearch(), equalTo("hsp:description"));
    }

    @Test
    @DisplayName("with-facet field")
    void whenDescriptionIsMapped_thenWidthFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getWidthFacet(), arrayContainingInAnyOrder(12F, 15.5F));
    }

    @Test
    @DisplayName("work-title-search field")
    void whenDescriptionIsMapped_thenWorkTitleSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescription("fixtures/loremIpsum_beschreibung.xml", mapHspObject("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getWorkTitleSearch(), arrayContainingInAnyOrder("Lorem ipsum"));
    }
  }

  @Nested
  @DisplayName("Group Mapping")
  class ObjectGroup {

    @Test
    @DisplayName("Injecting facet values with empty int values")
    void testInjectFacetFields_IntArray_EmptyKodValue() throws Exception {
      HspObjectGroup group = new HspObjectGroup();
      final HspObject obj = getHspObject();
      final HspDescription firstDesc = getHspDescription();
      final HspDescription secondDesc = getHspDescription();
      obj.setLeavesCountFacet(new Integer[]{1});
      firstDesc.setLeavesCountFacet(new Integer[]{2, 3});
      secondDesc.setLeavesCountFacet(new Integer[]{4});
      group.setHspObject(obj);
      group.setHspDescriptions(List.of(firstDesc, secondDesc));

      group = SolrMapper.injectFacetValuesJson(group);

      assertThat(group.getHspObject().getLeavesCountFacet(), arrayContainingInAnyOrder(1, 2, 3, 4));
      assertThat(group.getHspDescriptions().get(0).getLeavesCountFacet(), arrayContainingInAnyOrder(1, 2, 3, 4));
      assertThat(group.getHspDescriptions().get(1).getLeavesCountFacet(), arrayContainingInAnyOrder(1, 2, 3, 4));
    }

    @Test
    @DisplayName("Injecting facet values with string values")
    void testInjectFacetFields_String() throws Exception {
      HspObjectGroup group = new HspObjectGroup();
      group.setHspDescriptions(new ArrayList<>());
      final HspObject obj = getHspObject();

      final HspDescription firstDesc = getHspDescription();
      final HspDescription secondDesc = getHspDescription();

      obj.setFormatFacet(new String[]{"format kod"});
      firstDesc.setFormatFacet(new String[]{"format desc 1", "format desc 2"});
      secondDesc.setFormatFacet(new String[]{"format desc 3"});

      group.setHspObject(obj);
      group.setHspDescriptions(List.of(firstDesc, secondDesc));
      group = SolrMapper.injectFacetValuesJson(group);

      assertThat(group.getHspObject().getFormatFacet(), arrayContainingInAnyOrder("format kod", "format desc 1", "format desc 2", "format desc 3"));
      assertThat(group.getHspDescriptions().get(0).getFormatFacet(), arrayContainingInAnyOrder("format kod", "format desc 1", "format desc 2", "format desc 3"));
      assertThat(group.getHspDescriptions().get(1).getFormatFacet(), arrayContainingInAnyOrder("format kod", "format desc 1", "format desc 2", "format desc 3"));
    }

    @Test
    @DisplayName("Injecting facet values with empty string values")
    void testInjectFacetFields_StringArray_EmptyKodValue() throws Exception {
      HspObjectGroup group = new HspObjectGroup();
      group.setHspDescriptions(new ArrayList<>());
      final HspObject obj = getHspObject();

      final HspDescription firstDesc = getHspDescription();
      final HspDescription secondDesc = getHspDescription();

      firstDesc.setFormatFacet(new String[]{"format desc 1", "format desc 2"});
      secondDesc.setFormatFacet(new String[]{"format desc 3"});
      group.setHspObject(obj);
      group.setHspDescriptions(List.of(firstDesc, secondDesc));
      group = SolrMapper.injectFacetValuesJson(group);

      assertThat(group.getHspObject().getFormatFacet(), arrayContainingInAnyOrder("format desc 1", "format desc 2", "format desc 3"));
      assertThat(group.getHspDescriptions().get(0).getFormatFacet(), arrayContainingInAnyOrder("format desc 1", "format desc 2", "format desc 3"));
      assertThat(group.getHspDescriptions().get(1).getFormatFacet(), arrayContainingInAnyOrder("format desc 1", "format desc 2", "format desc 3"));
    }

    @Test
    @DisplayName("Injecting facet values - depth")
    void whenActivityStreamMessageIsMapped_thenDepthFacetIsCorrect() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), getDescriptionContents());

      assertThat(group.getHspObject().getDepthFacet(), arrayContainingInAnyOrder(2f, 3f));
      assertThat(group.getHspDescriptions().get(0).getDepthFacet(), arrayContainingInAnyOrder(2f, 3f));
      assertThat(group.getHspDescriptions().get(1).getDepthFacet(), arrayContainingInAnyOrder(2f, 3f));
    }

    @Test
    @DisplayName("Injecting facet values - format")
    void whenActivityStreamMessageIsMapped_thenFormatFacetIsCorrect() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), getDescriptionContents());

      assertThat(group.getHspObject().getFormatFacet(), arrayContainingInAnyOrder("folio", "oblong", "octavo"));
      assertThat(group.getHspDescriptions().get(0).getFormatFacet(), arrayContainingInAnyOrder("folio", "oblong", "octavo"));
      assertThat(group.getHspDescriptions().get(1).getFormatFacet(), arrayContainingInAnyOrder("folio", "oblong", "octavo"));
    }

    @Test
    @DisplayName("Injecting facet values - object type")
    void whenActivityStreamMessageIsMapped_thenFormatTypeFacetIsCorrect() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), getDescriptionContents());

      assertThat(group.getHspObject().getFormatTypeFacet(), arrayContainingInAnyOrder("factual", "deduced", "computed"));
      assertThat(group.getHspDescriptions().get(0).getFormatTypeFacet(), arrayContainingInAnyOrder("factual", "deduced", "computed"));
      assertThat(group.getHspDescriptions().get(1).getFormatTypeFacet(), arrayContainingInAnyOrder("factual", "deduced", "computed"));
    }

    @Test
    @DisplayName("Injecting facet values - height")
    void whenActivityStreamMessageIsMapped_thenHeightFacetIsCorrect() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), getDescriptionContents());

      assertThat(group.getHspObject().getHeightFacet(), arrayContainingInAnyOrder(12.5f, 16f, 17f));
      assertThat(group.getHspDescriptions().get(0).getHeightFacet(), arrayContainingInAnyOrder(12.5f, 16f, 17f));
      assertThat(group.getHspDescriptions().get(1).getHeightFacet(), arrayContainingInAnyOrder(12.5f, 16f, 17f));
    }

    @Test
    @DisplayName("Injecting facet values - leaves count")
    void whenActivityStreamMessageIsMapped_thenLeavesCountFacetIsCorrect() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), getDescriptionContents());

      assertThat(group.getHspObject().getLeavesCountFacet(), arrayContainingInAnyOrder(102, 103));
      assertThat(group.getHspDescriptions().get(0).getLeavesCountFacet(), arrayContainingInAnyOrder(102, 103));
      assertThat(group.getHspDescriptions().get(1).getLeavesCountFacet(), arrayContainingInAnyOrder(102, 103));
    }

    @Test
    @DisplayName("Injecting facet values - material")
    void whenActivityStreamMessageIsMapped_thenMaterialFacetIsCorrect() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), getDescriptionContents());

      assertThat(group.getHspObject().getMaterialFacet(), arrayContainingInAnyOrder("linen", "paper", "palm", "papyrus"));
      assertThat(group.getHspDescriptions().get(0).getMaterialFacet(), arrayContainingInAnyOrder("linen", "paper", "palm", "papyrus"));
      assertThat(group.getHspDescriptions().get(1).getMaterialFacet(), arrayContainingInAnyOrder("linen", "paper", "palm", "papyrus"));
    }

    @Test
    @DisplayName("Injecting facet values - object type")
    void whenActivityStreamMessageIsMapped_thenObjectTypeFacetIsCorrect() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), getDescriptionContents());

      assertThat(group.getHspObject().getObjectTypeFacet(), arrayContainingInAnyOrder("codex", "leporello"));
      assertThat(group.getHspDescriptions().get(0).getObjectTypeFacet(), arrayContainingInAnyOrder("codex", "leporello"));
      assertThat(group.getHspDescriptions().get(1).getObjectTypeFacet(), arrayContainingInAnyOrder("codex", "leporello"));
    }

    @Test
    @DisplayName("Injecting facet values - orig date when")
    void whenActivityStreamMessageIsMapped_thenOrigDateWhenFacetIsCorrect() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), getDescriptionContents());

      assertThat(group.getHspObject().getOrigDateWhenFacet(), arrayContainingInAnyOrder(1488, 1487));
      assertThat(group.getHspDescriptions().get(0).getOrigDateWhenFacet(), arrayContainingInAnyOrder(1488, 1487));
      assertThat(group.getHspDescriptions().get(1).getOrigDateWhenFacet(), arrayContainingInAnyOrder(1488, 1487));
    }

    @Test
    @DisplayName("Injecting facet values - status")
    void whenActivityStreamMessageIsMapped_thenStatusFacetIsCorrect() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), getDescriptionContents());

      assertThat(group.getHspObject().getStatusFacet(), arrayContainingInAnyOrder("existent"));
      assertThat(group.getHspDescriptions().get(0).getStatusFacet(), arrayContainingInAnyOrder("existent"));
      assertThat(group.getHspDescriptions().get(1).getStatusFacet(), arrayContainingInAnyOrder("existent"));
    }

    @Test
    @DisplayName("Injecting facet values - width")
    void whenActivityStreamMessageIsMapped_thenWidthFacetIsCorrect() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), getDescriptionContents());

      assertThat(group.getHspObject().getWidthFacet(), arrayContainingInAnyOrder(12f, 15.5f, 13f));
      assertThat(group.getHspDescriptions().get(0).getWidthFacet(), arrayContainingInAnyOrder(12f, 15.5f, 13f));
      assertThat(group.getHspDescriptions().get(1).getWidthFacet(), arrayContainingInAnyOrder(12f, 15.5f, 13f));
    }

    @Test
    void whenHspObjectWithoutHspDescriptionIsMapped_DescribedObjectFacetIsFalse() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), Collections.emptyList());

      assertThat(group.getHspObject()
              .getDescribedObjectFacet(), equalTo(Boolean.FALSE.toString()));
    }
  }

  @Nested
  @DisplayName("KOD Mapping")
  class Objects {
    @Test
    void whenGetMsIdentifierFromSettlementRepositorySortKeyOrIdnoIsCalledAndSortKeyExists_thenSortKeyIsUsed() {
      HspObject hspObject = new HspObject();
      hspObject.setIdnoSearch("MS 1234");
      hspObject.setIdnoSortKey("12345");
      hspObject.setSettlementDisplay("Leipzig");
      hspObject.setRepositoryDisplay("Universitätsbibliothek Leipzig");
      assertThat(SolrMapper.getMsIdentifierFromSettlementRepositoryIdno(hspObject), is("Leipzig, Universitätsbibliothek Leipzig, MS 1234"));
      assertThat(SolrMapper.getMsIdentifierFromSettlementRepositorySortKeyOrIdno(hspObject), equalTo("Leipzig, Universitätsbibliothek Leipzig, 12345"));
    }

    @Test
    void whenGetMsIdentifierFromSettlementRepositorySortKeyOrIdnoIsCalledAndSortKeyNotExists_thenIdnoIsUsed() {
      HspObject hspObject = new HspObject();
      hspObject.setIdnoSearch("MS 1234");
      hspObject.setSettlementDisplay("Leipzig");
      hspObject.setRepositoryDisplay("Universitätsbibliothek Leipzig");
      assertThat(SolrMapper.getMsIdentifierFromSettlementRepositoryIdno(hspObject), is("Leipzig, Universitätsbibliothek Leipzig, MS 1234"));
      assertThat(SolrMapper.getMsIdentifierFromSettlementRepositorySortKeyOrIdno(hspObject), is("Leipzig, Universitätsbibliothek Leipzig, MS 1234"));
    }

    @Test
    void whenHspObjectContainsOrigDateSortingInformation_thenAssociatedDescriptionWillAlsoDo() throws Exception {
      final HspObject loremIpsumDescription = mapHspObject("fixtures/loremIpsum_beschreibung.xml");

      assertThat(loremIpsumDescription.getOrigDateFromSort(), is(loremIpsumDescription.getOrigDateFromSort()));
      assertThat(loremIpsumDescription.getOrigDateToSort(), is(loremIpsumDescription.getOrigDateToSort()));
    }

    @Test
    void whenHspObjectIsMappedWithNormdatenService_origPlaceContainsAllNormdatumInformation() throws Exception {
      runWithNormdatenService(() -> {
        final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");
        assertThat(loremIpsumKOD.getOrigPlaceSearch(), arrayContainingInAnyOrder("5036103-X", "NORM-774909e2-f687-30cb-a5c4-ddc95806d6be", "DE-1", "soz_30002258", "Dt. SB", "Staatsbibliothek zu Berlin - Preußischer Kulturbesitz", "Gosudarstvennaja Biblioteka v Berline - Prusskoe Kulʹturnoe Nasledie", "SBPK", "Bibliotheca Regia Berolinensis", "Staatsbibliothek zu Berlin, Berlin", "Staatsbibliothek zu Berlin", "Berlin"));
        assertThat(loremIpsumKOD.getOrigPlaceDisplay(), is("Staatsbibliothek zu Berlin, Berlin"));
        assertThat(loremIpsumKOD.getOrigPlaceFacet(), arrayContainingInAnyOrder("Staatsbibliothek zu Berlin"));
      }, solrMapper);
    }

    @Test
    void whenHspObjectIsMappedWithoutNormdatenService_origPlaceContainsOnlyOrigPlaceFromIndexFieldAndDefaultValues() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigPlaceSearch(), arrayContainingInAnyOrder("Staatsbibliothek zu Berlin, Berlin", "Staatsbibliothek zu Berlin", "Berlin"));
      assertThat(loremIpsumKOD.getOrigPlaceDisplay(), is("Staatsbibliothek zu Berlin, Berlin"));
    }

    @Test
    void whenHspObjectIsMapped_thenDepthFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getDepthFacet(), is(new float[]{2}));
    }

    @Test
    void whenHspObjectIsMapped_thenDimensionsDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getDimensionsDisplay(), arrayContainingInAnyOrder("16 × 12 (Teil I)", "12,5 × 15,5 (Teil II)"));
    }

    @Test
    @DisplayName("format-facet field")
    void whenHspObjectIsMapped_thenFormatFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getFormatFacet(), arrayContaining("folio", "oblong"));
    }

    @Test
    void whenHspObjectIsMapped_thenFormatIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getFormatSearch(), arrayContainingInAnyOrder("folio", "oblong"));
      assertThat(loremIpsumKOD.getFormatFacet(), is(loremIpsumKOD.getFormatSearch()));
    }

    @Test
    @DisplayName("format-search field")
    void whenHspObjectIsMapped_thenFormatSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getFormatSearch(), arrayContainingInAnyOrder("folio", "oblong"));
    }

    @Test
    void whenHspObjectIsMapped_thenFormatTypeDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getFormatTypeDisplay(), arrayContainingInAnyOrder("deduced", "factual"));
    }

    @Test
    void whenHspObjectIsMapped_thenFormatTypeFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getFormatTypeFacet(), arrayContainingInAnyOrder("deduced", "factual"));
    }

    @Test
    void whenHspObjectIsMapped_thenHasNotationDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getHasNotationDisplay(), is("no"));
    }

    @Test
    void whenHspObjectIsMapped_thenHasNotationFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getHasNotationFacet(), arrayContainingInAnyOrder("no"));
    }

    @Test
    @DisplayName("height-facet field")
    void whenHspObjectIsMapped_thenHeightFacetIsCorrect() throws Exception {
      final HspObject loremIpsumDescription = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumDescription.getHeightFacet(), arrayContainingInAnyOrder(16F, 12.5F));
    }

    @Test
    void whenHspObjectIsMapped_thenIdIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getId(), is("__UUID__"));
    }

    @Test
    void whenHspObjectIsMapped_thenIdnoAltSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");
      assertThat(loremIpsumKOD.getIdnoAltSearch(), arrayContainingInAnyOrder("HSP-3d18a39c-1429-341e-b397-ca2bb8f9cdfb", "obj_123456", "dolor sit amet", "St. Emm 57"));
    }

    @Test
    void whenHspObjectIsMapped_thenIdnoSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");
      assertThat(loremIpsumKOD.getIdnoSearch(), is("Cod. ms. Bord. 1"));
    }

    @Test
    void whenHspObjectIsMapped_thenIlluminateDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getIlluminatedDisplay(), is("yes"));
    }

    @Test
    void whenHspObjectIsMapped_thenIlluminatedFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getIlluminatedFacet(), arrayContainingInAnyOrder("yes"));
    }

    @Test
    void whenHspObjectIsMapped_thenLanguageDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getLanguageDisplay(), arrayContainingInAnyOrder("de", "la"));
    }

    @Test
    void whenHspObjectIsMapped_thenLanguageFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getLanguageFacet(), arrayContainingInAnyOrder("de", "la"));
    }

    @Test
    void whenHspObjectIsMapped_thenLanguageSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getLanguageSearch(), arrayContainingInAnyOrder("latein und deutsch", "de", "la"));
    }

    @Test
    void whenHspObjectIsMapped_thenLastModifiedIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getLastModifiedDisplay(), DateMatchers.isDay(2022, Month.NOVEMBER, 23));
    }

    @Test
    @DisplayName("leaves-count-display field")
    void whenHspObjectIsMapped_thenLeavesCountDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getLeavesCountDisplay(), is("103 Bl., aus zwei Teilen zusammengesetzt"));
    }

    @Test
    @DisplayName("leaves-count-facet field")
    void whenHspObjectIsMapped_thenLeavesCountFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getLeavesCountFacet(), arrayContaining(103));
    }

    @Test
    @DisplayName("material-display field")
    void whenHspObjectIsMapped_thenMaterialDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getMaterialDisplay(), is("Mischung aus Papier und Leinen"));
    }

    @Test
    @DisplayName("material-facet field")
    void whenHspObjectIsMapped_thenMaterialFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getMaterialFacet(), arrayContainingInAnyOrder("linen", "paper"));
    }

    @Test
    @DisplayName("material-search field")
    void whenHspObjectIsMapped_thenMaterialSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getMaterialSearch(), arrayContaining("Mischung aus Papier und Leinen", "linen", "paper"));
    }

    @Test
    @DisplayName("object-type-facet field")
    void whenHspObjectIsMapped_thenObjectTypeFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getObjectTypeFacet(), arrayContainingInAnyOrder("codex"));
    }

    @Test
    @DisplayName("object-type-search field")
    void whenHspObjectIsMapped_thenObjectTypeSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getObjectTypeSearch(), is("codex"));
    }

    @Test
    @DisplayName("orig-date-from-facet field")
    void whenHspObjectIsMapped_thenOrigDateFromFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigDateFromFacet(), arrayContainingInAnyOrder(1651, -14));
    }

    @Test
    @DisplayName("orig-date-from-search field")
    void whenHspObjectIsMapped_thenOrigDateFromSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigDateFromSearch(), arrayContainingInAnyOrder(1651, -14));
    }

    @Test
    @DisplayName("orig-date-type-facet field")
    void whenHspObjectIsMapped_thenOrigDateTypeFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigDateTypeFacet(), arrayContainingInAnyOrder("datable", "dated"));
    }

    @Test
    @DisplayName("orig-date-to-facet field")
    void whenHspObjectIsMapped_thenOrigDateToFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigDateToFacet(), arrayContainingInAnyOrder(15, 1680));
    }

    @Test
    @DisplayName("orig-date-to-search field")
    void whenHspObjectIsMapped_thenOrigDateToSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigDateToSearch(), arrayContainingInAnyOrder(15, 1680));
    }

    @Test
    @DisplayName("orig-date-when-facet field")
    void whenHspObjectIsMapped_thenOrigDateWhenFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigDateWhenFacet(), arrayContainingInAnyOrder(1488));
    }

    @Test
    @DisplayName("orig-date-when-search field")
    void whenHspObjectIsMapped_thenOrigDateWhenSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigDateWhenSearch(), arrayContainingInAnyOrder("1488", "um 1665 (Teil II)", "um 0 (Teil III)"));
    }

    @Test
    @DisplayName("orig-place-display field")
    void whenHspObjectIsMapped_thenOrigPlaceDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigPlaceDisplay(), is("Staatsbibliothek zu Berlin, Berlin"));
    }

    @Test
    @DisplayName("orig-place-facet field")
    void whenHspObjectIsMapped_thenOrigPlaceFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigPlaceFacet(), arrayContainingInAnyOrder("Staatsbibliothek zu Berlin", "Berlin"));
    }

    @Test
    @DisplayName("orig-place-search field")
    void whenHspObjectIsMapped_thenOrigPlaceSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigPlaceSearch(), arrayContainingInAnyOrder("Staatsbibliothek zu Berlin, Berlin", "Staatsbibliothek zu Berlin", "Berlin"));
    }

    @Test
    @DisplayName("persistent-url-display field")
    void whenHspObjectIsMapped_thenPersistentURLDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getPersistentURLDisplay(), is("https://resolver.staatsbibliothek-berlin.de/__UUID__"));
    }

    @Test
    @DisplayName("repository-display field")
    void whenHspObjectIsMapped_thenRepositoryDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getRepositoryDisplay(), is("Universitätsbibliothek"));
    }

    @Test
    @DisplayName("repository-facet field")
    void whenHspObjectIsMapped_thenRepositoryFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getRepositoryFacet(), is(nullValue()));
    }

    @Test
    @DisplayName("repository-search field")
    void whenHspObjectIsMapped_thenRepositorySearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getRepositorySearch(), arrayContainingInAnyOrder("Universitätsbibliothek"));
    }

    @Test
    @DisplayName("settlement-display field")
    void whenHspObjectIsMapped_thenSettlementDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getSettlementDisplay(), is("Kiel"));
    }

    @Test
    @DisplayName("settlement-facet field")
    void whenHspObjectIsMapped_thenSettlementFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getSettlementFacet(), is(nullValue()));
    }

    @Test
    @DisplayName("settlement-search field")
    void whenHspObjectIsMapped_thenSettlementSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getSettlementSearch(), arrayContainingInAnyOrder("Kiel"));
    }

    @Test
    @DisplayName("status-facet field")
    void whenHspObjectIsMapped_thenStatusFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getStatusFacet(), arrayContainingInAnyOrder("existent"));
    }

    @Test
    @DisplayName("status-search field")
    void whenHspObjectIsMapped_thenStatusSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getStatusSearch(), is("existent"));
    }

    @Test
    @DisplayName("tei-document-display field")
    void whenHspObjectIsMapped_thenTEIDocumentDisplayIsCorrect() throws Exception {
      final byte[] tei = dataFromResourceFilename("fixtures/loremIpsum_beschreibung.xml");
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_beschreibung.xml");

      assertThat(loremIpsumKOD.getTeiDocumentDisplay(), is(new String(tei)));
    }

    @Test
    @DisplayName("title-search field")
    void whenHspObjectIsMapped_thenTitleSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getTitleSearch(), equalTo("Catalogi bibliothecae Bordesholmensis, Bordesholmer Handschriften"));
    }

    @Test
    @DisplayName("type-search field")
    void whenHspObjectIsMapped_thenTypeSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getTypeSearch(), equalTo("hsp:object"));
    }

    @Test
    @DisplayName("width-facet field")
    void whenHspObjectIsMapped_thenWidthFacetIsCorrect() throws Exception {
      final HspObject loremIpsumDescription = mapHspObject("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumDescription.getWidthFacet(), arrayContainingInAnyOrder(12F, 15.5F));
    }

    @Test
    void whenTEIDoesNotContainOrigDateDated_thenOrigDateFacetIsEmpty() throws Exception {
      final HspObject loremIpsumKODModified = mapHspObject("fixtures/loremIpsum_kod_modifiziert.xml");

      assertThat(loremIpsumKODModified.getOrigDateWhenFacet(), nullValue());
    }
  }
}