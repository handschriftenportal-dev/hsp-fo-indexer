package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.xml;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.AuthorityFileService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.GraphQlService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.impl.AuthorityFileServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.impl.InMemoryAuthorityFileRepository;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.SolrMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.entity.MappingTestModel;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.model.ClassField;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.model.ClassModel;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.PostProcessor;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.converter.ClassToClassModelConverter;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.TestDataFactory;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.fixture.AuthorityFileFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.fixture.FileFixture.dataFromResourceFilename;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class XMLMapperTest {
  private XMLMapper xmlMapper;
  private SolrMapper solrMapper;

  public XMLMapperTest() {
    this.solrMapper = new SolrMapper();
    PostProcessor postProcessor = new PostProcessor("de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes", getClass().getClassLoader());
    this.xmlMapper = new XMLMapper((postProcessors, node, value) -> postProcessor.runPostProcessing(postProcessors, node, value, AuthorityFileFixture.getAuthorityFileService()));
  }

  @Nested
  class PostProcessorTest {
    @Test
    void whenMappingWithCorporatePreferredProcessorByKey_thenResultingValueContainsOnlyPreferredName() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/12505_tei_kod_with_authority_file_references.xml");
      ClassModel classModel = ClassToClassModelConverter.convert(MappingTestModel.class);
      ClassField classField = classModel.getFields().stream().filter(cf -> cf.getName().equals("corporatePreferredPostProcessorWithAuthorityFileReference")).findFirst().get();
      AuthorityFileFixture.runWithAuthorityFileService(() -> {
        final List<String> result = xmlMapper.getFieldValue(classField, doc);
        assertThat(result, hasSize(1));
        assertThat(result.get(0), is("test preferred name"));
      }, solrMapper);
    }

    @Test
    void whenMappingWithCorporatePreferredProcessorWithoutAuthorityFileReferenceButDefaultValue_thenResultIsDefaultValue() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/14025_description_with_gnd_references.xml");
      ClassModel classModel = ClassToClassModelConverter.convert(MappingTestModel.class);
      ClassField classField = classModel.getFields().stream().filter(cf -> cf.getName().equals("corporatePreferredPostProcessorWithoutAuthorityFileReferenceWithDefault")).findFirst().get();

      AuthorityFileFixture.runWithAuthorityFileService(() -> {
        final List<String> result = xmlMapper.getFieldValue(classField, doc);
        assertThat(result, hasSize(1));
        assertThat(result, hasItem("Staatsbibliothek zu Berlin"));
      }, solrMapper);
    }

    @Test
    void whenMappingWithCorporateProcessor_thenResultingValueContainsAllTheAuthorityFileInformation() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/12505_tei_kod_with_authority_file_references.xml");
      ClassModel classModel = ClassToClassModelConverter.convert(MappingTestModel.class);
      ClassField classField = classModel.getFields().stream().filter(cf -> cf.getName().equals("corporatePostProcessorWithAuthorityFileReference")).findFirst().get();

      AuthorityFileFixture.runWithAuthorityFileService(() -> {
        final List<String> result = xmlMapper.getFieldValue(classField, doc);
        assertThat(result, hasSize(7));
        assertThat(result, hasItem("NORM-909a19c7-84f8-4151-8702-5cd384b2dd7e"));
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
      ClassModel classModel = ClassToClassModelConverter.convert(MappingTestModel.class);
      ClassField classField = classModel.getFields().stream().filter(cf -> cf.getName().equals("placePostProcessorWithAuthorityFileReference")).findFirst().get();

      AuthorityFileFixture.runWithAuthorityFileService(() -> {
        final List<String> result = xmlMapper.getFieldValue(classField, doc);
        assertThat(result, containsInAnyOrder("5036103-X", "NORM-774909e2-f687-30cb-a5c4-ddc95806d6be", "DE-1", "soz_30002258", "Dt. SB", "Staatsbibliothek zu Berlin - Preußischer Kulturbesitz", "Gosudarstvennaja Biblioteka v Berline - Prusskoe Kulʹturnoe Nasledie", "SBPK", "Bibliotheca Regia Berolinensis", "Staatsbibliothek zu Berlin"));
      }, solrMapper);
    }

    @Test
    void whenMappingWithPlaceProcessor_thenResultingValueContainsPlacesInformation() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/loremIpsum_beschreibung.xml");
      ClassModel classModel = ClassToClassModelConverter.convert(MappingTestModel.class);
      ClassField classField = classModel.getFields().stream().filter(cf -> cf.getName().equals("placePostProcessorWithAuthorityFileReference")).findFirst().get();

      final GraphQlService graphQlService = new GraphQlService(WebClient.builder(), "localhost", "/rest/graphql", 56789, "http");
      final AuthorityFileService authorityFileService = new AuthorityFileServiceImpl(graphQlService, new InMemoryAuthorityFileRepository());
      solrMapper.setAuthorityFileService(authorityFileService);

      AuthorityFileFixture.runWithAuthorityFileService(() -> {
        final List<String> result = xmlMapper.getFieldValue(classField, doc);
        assertThat(result, hasItems("4030481-4", "NORM-1a75b353-14be-3c19-b22c-5c5334d78c8f", "Kiel"));
      }, solrMapper);
    }

    @Test
    void whenMappingWithPreferredPlaceProcessorWithoutAuthorityFileReference_thenResultIsDefaultValue() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/14025_description_with_gnd_references.xml");
      ClassModel classModel = ClassToClassModelConverter.convert(MappingTestModel.class);
      ClassField classField = classModel.getFields().stream().filter(cf -> cf.getName().equals("placePreferredPostProcessorWithoutNormdatumWithDefaultValue")).findFirst().get();

      AuthorityFileFixture.runWithAuthorityFileService(() -> {
        final List<String> result = xmlMapper.getFieldValue(classField, doc);
        assertThat(result, hasSize(1));
        assertThat(result, hasItem("Staatsbibliothek zu Berlin"));
      }, solrMapper);
    }

    @Test
    void whenMappingWithPreferredPlaceProcessor_thenResultingValueContainsOnlyPlacesPreferredName() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/loremIpsum_beschreibung.xml");
      ClassModel classModel = ClassToClassModelConverter.convert(MappingTestModel.class);
      ClassField classField = classModel.getFields().stream().filter(cf -> cf.getName().equals("placePreferredPostProcessorWithAuthorityFileReference")).findFirst().get();

      AuthorityFileFixture.runWithAuthorityFileService(() -> {
        final List<String> result = xmlMapper.getFieldValue(classField, doc);
        assertThat(result, hasItems("Kiel"));
      }, solrMapper);
    }

    @Test
    void whenMappingWithPreferredPlaceProcessor_thenResultingValueContainsPreferredNames() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/14025_description_with_gnd_references.xml");
      ClassModel classModel = ClassToClassModelConverter.convert(MappingTestModel.class);
      ClassField classField = classModel.getFields().stream().filter(cf -> cf.getName().equals("placePreferredPostProcessorWithAuthorityFileReference")).findFirst().get();

      AuthorityFileFixture.runWithAuthorityFileService(() -> {
        final List<String> result = xmlMapper.getFieldValue(classField, doc);
        assertThat(result, hasSize(1));
        assertThat(result, containsInAnyOrder("Staatsbibliothek zu Berlin"));
      }, solrMapper);
    }

    @Test
    void whenMappingWithPreferredPlaceProcessorAndMissingAuthorityFileReference_thenResultingValueIsEmpty() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/14025_description_with_gnd_references.xml");
      ClassModel classModel = ClassToClassModelConverter.convert(MappingTestModel.class);
      ClassField classField = classModel.getFields().stream().filter(cf -> cf.getName().equals("placePreferredPostProcessorWithoutAuthorityFileReference")).findFirst().get();

      AuthorityFileFixture.runWithAuthorityFileService(() -> {
        final List<String> result = xmlMapper.getFieldValue(classField, doc);
        assertThat(result, hasSize(0));
      }, solrMapper);
    }

    @Test
    void givenNotExistentAuthorityFileId_whenMappingWithPersonPostProcessor_thenResultsContainsAttributeValueAndDefaultValue() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/loremIpsum_beschreibung.xml");
      ClassModel classModel = ClassToClassModelConverter.convert(MappingTestModel.class);
      ClassField classField = classModel.getFields().stream().filter(cf -> cf.getName().equals("personPostProcessorWithoutAuthorityFileReferenceAndDefaultValue")).findFirst().get();

      AuthorityFileFixture.runWithAuthorityFileService(() -> {
        final List<String> result = xmlMapper.getFieldValue(classField, doc);
        assertThat(result, hasSize(2));
        assertThat(result, containsInAnyOrder("Konstantin Görlitz", "NORM-invalide-id"));
      }, solrMapper);
    }
  }
  @Test
  void whenAdditionalValueIsSet_thenAdditionalValueIsUsed() throws Exception {
    final byte[] doc = dataFromResourceFilename("fixtures/14025_description_with_gnd_references.xml");

    ClassModel classModel = ClassToClassModelConverter.convert(MappingTestModel.class);
    ClassField classField = classModel.getFields().stream().filter(cf -> cf.getName().equals("origPlaceKeyWithAdditionalValue")).findFirst().get();

    AuthorityFileFixture.runWithAuthorityFileService(() -> {
      final List<String> result = xmlMapper.getFieldValue(classField, doc);
      assertThat(result, hasSize(2));
      assertThat(result, hasItems("Großbritannien, Nordharz", "a9862391-674b-4383-9721-a6eb63ae6cb4"));
    }, solrMapper);
  }
  @Nested
  @DisplayName("Annotation based mapping")
  class XMLSourceEvaluation {

    @Test
    @DisplayName("Multiple XMLSources with attribute xPath, multivalued")
    void testGetAttributeValue_MultiNode_MultiValued() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
      ClassModel classModel = ClassToClassModelConverter.convert(MappingTestModel.class);
      ClassField classField = classModel.getFields().stream().filter(cf -> cf.getName().equals("testAttr_multiNode_multiSelector_multiValue")).findFirst().get();

      final List<String> values = xmlMapper.getFieldValue(classField, doc);

      assertThat(values, notNullValue());
      assertThat(values.size(), equalTo(2));
      assertThat(values, hasItem("29,5"));
      assertThat(values, hasItem("14"));
    }

    @Test
    @DisplayName("Multiple XMLSources with attribute xPath")
    void testGetAttributeValue_MultiNode_SingleValued() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
      ClassModel classModel = ClassToClassModelConverter.convert(MappingTestModel.class);
      ClassField classField = classModel.getFields().stream().filter(cf -> cf.getName().equals("testAttr_multiNode_singleSelector")).findFirst().get();

      final List<String> values = xmlMapper.getFieldValue(classField, doc);

      assertThat(values, notNullValue());
      assertThat(values.size(), equalTo(1));
      assertThat(values.get(0), equalTo("14"));
    }

    @Test
    @DisplayName("Multiple XMLSources with attribute xPath, with first XMLSource empty")
    void testGetAttributeValue_MultiNode_SingleValued_firstSelectorIsEmpty() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
      ClassModel classModel = ClassToClassModelConverter.convert(MappingTestModel.class);
      ClassField classField = classModel.getFields().stream().filter(cf -> cf.getName().equals("testAttr_multiNode_singleSelector_firstEmpty")).findFirst().get();

      final List<String> values = xmlMapper.getFieldValue(classField, doc);

      assertThat(values, notNullValue());
      assertThat(values.size(), equalTo(1));
      assertThat(values.get(0), equalTo("29,5"));
    }

    @Test
    @DisplayName("XMLSource with attribute xPath and multiValue")
    void testGetAttributeValue_MultiValued() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
      ClassModel classModel = ClassToClassModelConverter.convert(MappingTestModel.class);
      ClassField classField = classModel.getFields().stream().filter(cf -> cf.getName().equals("testAttrSingleSelectorMulti")).findFirst().get();

      final List<String> values = xmlMapper.getAttributeFieldValue(classField.getSources().get(0), doc);

      assertThat(values, notNullValue());
      assertThat(values.size(), equalTo(2));
      assertThat(values.get(0), equalTo("com"));
      assertThat(values.get(1), equalTo("org"));
    }

    @Test
    @DisplayName("XMLSource with attribute xPath")
    void testGetAttributeValue_SingleValued() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
      ClassModel classModel = ClassToClassModelConverter.convert(MappingTestModel.class);
      ClassField classField = classModel.getFields().stream().filter(cf -> cf.getName().equals("testAttrSingleSelector")).findFirst().get();

      final List<String> values = xmlMapper.getAttributeFieldValue(classField.getSources().get(0), doc);

      assertThat(values, notNullValue());
      assertThat(values.size(), equalTo(1));
      assertThat(values.get(0), equalTo("person"));
    }

    @Test
    @DisplayName("XMLSource with multiple attribute xPath")
    void testGetAttributeValue_SingleValued_MultiXPathExpressions() throws Exception {
      final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
      ClassModel classModel = ClassToClassModelConverter.convert(MappingTestModel.class);
      ClassField classField = classModel.getFields().stream().filter(cf -> cf.getName().equals("testAttrMultiSelector")).findFirst().get();

      final List<String> values = xmlMapper.getAttributeFieldValue(classField.getSources().get(0), doc);

      assertThat(values, notNullValue());
      assertThat(values.size(), equalTo(3));
      assertThat(values.get(0), equalTo("person"));
      assertThat(values.get(1), equalTo("com"));
      assertThat(values.get(2), equalTo("org"));
    }
  }

  @Test
  void whenXpathHits_thenDefaultValueIsNotPartOfResult() throws Exception {
    final byte[] doc = dataFromResourceFilename("fixtures/14025_description_with_gnd_references.xml");
    ClassModel classModel = ClassToClassModelConverter.convert(MappingTestModel.class);
    ClassField classField = classModel.getFields().stream().filter(cf -> cf.getName().equals("origPlaceKeyWithDefaultValue")).findFirst().get();

    AuthorityFileFixture.runWithAuthorityFileService(() -> {
      final List<String> result = xmlMapper.getFieldValue(classField, doc);
      assertThat(result, hasSize(1));
      assertThat(result, hasItem("a9862391-674b-4383-9721-a6eb63ae6cb4"));
    }, solrMapper);
  }

  @Test
  void whenXpathMisses_thenDefaultValueIsUsed() throws Exception {
    final byte[] doc = dataFromResourceFilename("fixtures/14025_description_with_gnd_references.xml");
    ClassModel classModel = ClassToClassModelConverter.convert(MappingTestModel.class);
    ClassField classField = classModel.getFields().stream().filter(cf -> cf.getName().equals("emptyOrigPlaceWithDefaultValue")).findFirst().get();

    AuthorityFileFixture.runWithAuthorityFileService(() -> {
      final List<String> result = xmlMapper.getFieldValue(classField, doc);
      assertThat(result, hasSize(1));
      assertThat(result, hasItem("Großbritannien, Nordharz"));
    }, solrMapper);
  }

  @Test
  @DisplayName("XMLSource with text node xPath and multiValue")
  void testGetTextFieldValue_MultiValued() throws Exception {
    final byte[] xml = dataFromResourceFilename("fixtures/tei_odd.xml");
    final String xPathExpression = "//tei:teiHeader/tei:fileDesc/tei:editionStmt/tei:respStmt/tei:name";

    final List<String> res = xmlMapper.getFieldValue(TestDataFactory.createClassField(xPathExpression, true), xml);

    assertThat(res, notNullValue());
    assertThat(res.size(), equalTo(2));
    assertThat(res.get(0), equalTo("Bertram Lesser"));
    assertThat(res.get(1), equalTo("Herzog August Bibliothek"));
  }

  @Test
  @DisplayName("XMLSource with text node xPath")
  void testGetTextFieldValue_SingleValued() throws Exception {
    final byte[] xml = dataFromResourceFilename("fixtures/tei_odd.xml");
    final String xPathExpression = "//tei:teiHeader/tei:fileDesc/tei:editionStmt/tei:respStmt/tei:name";

    final List<String> res = xmlMapper.getFieldValue(TestDataFactory.createClassField(xPathExpression, false), xml);

    assertThat(res, notNullValue());
    assertThat(res.size(), equalTo(1));
    assertThat(res.get(0), equalTo("Bertram Lesser"));
  }
}
