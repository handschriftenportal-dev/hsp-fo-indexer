package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.kafka;

import static de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.matcher.ArrayContainsElementsMatcher.containsElements;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.ArrayMatching.arrayContainingInAnyOrder;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.AuthorityFileService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.GraphQlService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.impl.AuthorityFileServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.impl.InMemoryAuthorityFileRepository;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.SolrMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspCatalog;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspDigitized;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspObject;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.GenericRepositoryServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.HspObjectType;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.SolrRepositoryImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.entity.impl.HspCatalogService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.entity.impl.HspObjectGroupService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.replication.ReplicationAdminService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.ActivityMessageHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.extension.MocoExtensionClassLevel;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.extension.MocoExtensionFileParam;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.fixture.Fixtures;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.mapper.ObjectMapperFactory;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.model.ActivityStreamMessage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.annotation.concurrent.NotThreadSafe;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.web.reactive.function.client.WebClient;

@ActiveProfiles("integration")
@ExtendWith(SpringExtension.class)
@NotThreadSafe
class SolrKafkaMessageHandlerTest {

  static SolrMapper solrMapper;
  static ObjectMapper objectMapper;
  private final ResourceLoader resourceLoader;
  private SolrKafkaMessageHandler messageHandler;

  private GenericRepositoryServiceImpl<HspObject> mockedHspObjService;
  private GenericRepositoryServiceImpl<HspDescription> mockedDescriptionService;
  private GenericRepositoryServiceImpl<HspDigitized> mockedDigitizedService;
  private GenericRepositoryServiceImpl<HspCatalog> mockedCatalogueService;

  @Mock
  ReplicationAdminService mockedReplicationAdminService;
  @Mock
  SolrRepositoryImpl<HspObject> mockedHspObjRepo;
  @Mock
  SolrRepositoryImpl<HspDescription> mockedHspDescRepo;
  @Mock
  SolrRepositoryImpl<HspDigitized> mockedHspDigitizedRepo;
  @Mock
  SolrRepositoryImpl<HspCatalog> mockedCatalogueRepo;

  @Captor
  private ArgumentCaptor<HspObject> objectCaptor;
  @Captor
  private ArgumentCaptor<ArrayList<HspDigitized>> digitizedCaptor;
  @Captor
  private ArgumentCaptor<ArrayList<HspDescription>> descriptionCaptor;
  @Captor
  private ArgumentCaptor<HspCatalog> catalogueCaptor;

  public SolrKafkaMessageHandlerTest(@Autowired final ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @BeforeAll
  static void setup() {
    solrMapper = new SolrMapper();
    objectMapper = ObjectMapperFactory.getObjectMapper();
  }

  @BeforeEach
  void init() {
    mockedHspObjService = Mockito.spy(new GenericRepositoryServiceImpl<>(mockedHspObjRepo, HspObjectType.OBJECT));
    mockedDescriptionService = Mockito.spy(
        new GenericRepositoryServiceImpl<>(mockedHspDescRepo, HspObjectType.DESCRIPTION));
    mockedDigitizedService = Mockito.spy(
        new GenericRepositoryServiceImpl<>(mockedHspDigitizedRepo, HspObjectType.DIGITIZATION));
    mockedCatalogueService = Mockito.spy(
        new GenericRepositoryServiceImpl<>(mockedCatalogueRepo, HspObjectType.CATALOG));

    final HspObjectGroupService hspObjectGroupService = new HspObjectGroupService(mockedHspObjService,
        mockedDescriptionService, mockedDigitizedService);
    final HspCatalogService hspCatalogService = new HspCatalogService(mockedCatalogueService);

    messageHandler = new SolrKafkaMessageHandler(solrMapper, hspObjectGroupService, hspCatalogService,
        mockedReplicationAdminService);
  }

  @DisplayName("HspObjectGroup mapping")
  @Nested
  @ExtendWith(MocoExtensionClassLevel.class)
  @MocoExtensionFileParam("moco/authorityfiles.json")
  class HspObjectGroup {

    @BeforeEach
    void setUp() throws Exception {
      HttpGraphQlClient client = HttpGraphQlClient.builder(WebClient.builder().baseUrl("http://localhost:56789/rest/graphql").build()).build();
      final GraphQlService graphQlService = new GraphQlService(client);
      final AuthorityFileService authorityFileService = new AuthorityFileServiceImpl(graphQlService,
          new InMemoryAuthorityFileRepository());
      solrMapper.setAuthorityFileService(authorityFileService);
      final Resource kodRes = resourceLoader.getResource("fixtures/loremIpsum_kod_digitalisat_iiif.xml");
      final Resource descRes = resourceLoader.getResource("fixtures/loremIpsum_beschreibung.xml");

      final ActivityStreamMessage asm = ActivityMessageHelper.fromResource(kodRes, descRes);
      messageHandler.handleMessage(asm);
    }

    @Test
    void givenASM_whenMapping_HspObjectIdIsCorrect() throws Exception {
      verify(mockedHspObjService).save(objectCaptor.capture());
      final HspObject capObj = objectCaptor.getValue();

      assertThat(capObj.getId(), equalTo("4de2ec4a-09e0-11ee-be56-0242ac120002"));
    }

    @Test
    void givenASM_whenMapping_thenHspObjectGroupIdIsCorrect() throws Exception {
      verify(mockedHspObjService).save(objectCaptor.capture());
      final HspObject capObj = objectCaptor.getValue();

      assertThat(capObj.getGroupIdSearch(), equalTo("4de2ec4a-09e0-11ee-be56-0242ac120002"));
    }

    @Test
    void givenASM_whenMapping_thenHspObjectTypeSearchIsCorrect() throws Exception {
      verify(mockedHspObjService).save(objectCaptor.capture());
      final HspObject capObj = objectCaptor.getValue();

      assertThat(capObj.getTypeSearch(), equalTo("hsp:object"));
    }

    @Test
    void givenASM_whenMapping_thenHspObjectIdnoSearchIsCorrect() throws Exception {
      verify(mockedHspObjService).save(objectCaptor.capture());
      final HspObject capObj = objectCaptor.getValue();

      assertThat(capObj.getIdnoSearch(), equalTo("Cod. ms. Bord. 1"));
    }

    @Test
    void givenASM_whenMapping_thenHspObjectDescribedObjectSearchIsCorrect() throws Exception {
      verify(mockedHspObjService).save(objectCaptor.capture());
      final HspObject capObj = objectCaptor.getValue();

      assertThat(capObj.getDescribedObjectSearch(), equalTo(Boolean.TRUE));
    }

    @Test
    void givenASM_whenMapping_thenHspObjectDigitizedObjectSearchIsCorrect() throws Exception {
      verify(mockedHspObjService).save(objectCaptor.capture());
      final HspObject capObj = objectCaptor.getValue();

      assertThat(capObj.getDigitizedObjectSearch(), equalTo(Boolean.TRUE));
    }

    @Test
    void givenASM_whenMapping_thenHspObjectPersistentURLDisplayIsCorrect() throws Exception {
      verify(mockedHspObjService).save(objectCaptor.capture());
      final HspObject capObj = objectCaptor.getValue();

      assertThat(capObj.getPersistentURLDisplay(), is("https://resolver.staatsbibliothek-berlin.de/__UUID__"));
    }

    @Test
    void givenASM_whenMapping_thenHspObjectSettlementDisplayIsCorrect() throws Exception {
      verify(mockedHspObjService).save(objectCaptor.capture());
      final HspObject capObj = objectCaptor.getValue();

      assertThat(capObj.getSettlementDisplay(), is("Kiel"));
    }

    @Test
    void givenASM_whenMapping_thenHspObjectSettlementFacetIsCorrect() throws Exception {
      verify(mockedHspObjService).save(objectCaptor.capture());
      final HspObject capObj = objectCaptor.getValue();

      assertThat(capObj.getSettlementFacet(), arrayContainingInAnyOrder("Kiel"));
    }

    @Test
    void givenASM_whenMapping_thenHspObjectSettlementSearchIsCorrect() throws Exception {
      verify(mockedHspObjService).save(objectCaptor.capture());
      final HspObject capObj = objectCaptor.getValue();

      assertThat(capObj.getSettlementSearch(),
          arrayContainingInAnyOrder("4030481-4", "NORM-1a75b353-14be-3c19-b22c-5c5334d78c8f", "Kiel"));
    }

    @Test
    void givenASM_whenMapping_thenHspObjectSettlementAuthorityFileDisplayIsCorrect() throws Exception {
      verify(mockedHspObjService).save(objectCaptor.capture());
      final HspObject capObj = objectCaptor.getValue();

      assertThat(capObj.getSettlementAuthorityFileDisplay(),
          arrayContainingInAnyOrder("NORM-1a75b353-14be-3c19-b22c-5c5334d78c8f"));
    }

    @Test
    void givenASM_whenMapping_thenHspObjectRepositoryDisplayIsCorrect() throws Exception {
      verify(mockedHspObjService).save(objectCaptor.capture());
      final HspObject capObj = objectCaptor.getValue();

      assertThat(capObj.getRepositoryDisplay(), is("Universitätsbibliothek Kiel"));
    }

    @Test
    void givenASM_whenMapping_thenHspObjectRepositoryFacetIsCorrect() throws Exception {
      verify(mockedHspObjService).save(objectCaptor.capture());
      final HspObject capObj = objectCaptor.getValue();

      assertThat(capObj.getRepositoryFacet(), arrayContainingInAnyOrder("Universitätsbibliothek Kiel"));
    }

    @Test
    void givenASM_whenMapping_thenHspObjectRepositorySearchIsCorrect() throws Exception {
      verify(mockedHspObjService).save(objectCaptor.capture());
      final HspObject capObj = objectCaptor.getValue();

      assertThat(capObj.getRepositorySearch(),
          arrayContainingInAnyOrder("36197-5", "NORM-9caa05ee-be18-3003-bfa9-454aa6349a9f",
              "Universitätsbibliothek Kiel", "Universität Kiel. Bibliothek",
              "Christian-Albrechts-Universität zu Kiel. Universitätsbibliothek Kiel",
              "Christian-Albrechts-Universität. Universitätsbibliothek", "Universität Kiel. Universitätsbibliothek",
              "Königliche Universitätsbibliothek", "Christian-Albrechts-Universität zu Kiel. Zentralbibliothek",
              "Christian-Albrecht-Universität zu Kiel. Universitätsbibliothek", "Universitätsbibliothek",
              "Kieler Universitätsbibliothek", "soz_30002611", "DE-8"));
    }

    @Test
    void givenASM_whenMapping_thenHspObjectRepositoryAuthorityFileDisplayIsCorrect() throws Exception {
      verify(mockedHspObjService).save(objectCaptor.capture());
      final HspObject capObj = objectCaptor.getValue();

      assertThat(capObj.getRepositoryAuthorityFileDisplay(),
          arrayContainingInAnyOrder("NORM-9caa05ee-be18-3003-bfa9-454aa6349a9f"));
    }

    @Test
    void givenASM_whenMapping_thenHspObjectMsIdentifierSearchIsCorrect() throws Exception {
      verify(mockedHspObjService).save(objectCaptor.capture());
      final HspObject capObj = objectCaptor.getValue();

      assertThat(capObj.getMsIdentifierSearch(), is("Kiel, Universitätsbibliothek Kiel, Cod. ms. Bord. 1"));
    }

    @Test
    void givenASM_whenMapping_thenHspObjectMsIdentifierSortIsCorrect() throws Exception {
      verify(mockedHspObjService).save(objectCaptor.capture());
      final HspObject capObj = objectCaptor.getValue();

      assertThat(capObj.getMsIdentifierSort(), is("Kiel, Universitätsbibliothek Kiel, Kiel_UB_Cod-Ms-Bord-001"));
    }

    @Test
    void givenASM_whenMapping_thenHspObjectOrigPlaceAuthorityFileDisplayIsCorrect() throws Exception {
      verify(mockedHspObjService).save(objectCaptor.capture());
      final HspObject capObj = objectCaptor.getValue();

      assertThat(capObj.getOrigPlaceAuthorityFileDisplay(),
          arrayContainingInAnyOrder("NORM-ee1611b6-1f56-38e7-8c12-b40684dbb395"));
    }

    @Test
    void whenHspObjectIsMapped_thenInstitutionPreviouslyOwningIsCorrect() throws Exception {
      verify(mockedHspObjService).save(objectCaptor.capture());
      final HspObject capObj = objectCaptor.getValue();

      assertThat(capObj.getInstitutionPreviouslyOwningSearch(),
          arrayContainingInAnyOrder("Kloster Sankt Emmeram Regensburg"));
    }

    @Test
    void whenHspObjectIsMapped_thenFormerIdNoSearchIsCorrect() throws Exception {
      verify(mockedHspObjService).save(objectCaptor.capture());
      final HspObject capObj = objectCaptor.getValue();

      assertThat(capObj.getFormerIdnoSearch(), arrayContainingInAnyOrder("St. Emm 57"));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionIdIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getId(), equalTo("__UUID__"));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionGroupIdSearchIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getGroupIdSearch(), equalTo("4de2ec4a-09e0-11ee-be56-0242ac120002"));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionTypeSearchIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getTypeSearch(), equalTo("hsp:description"));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionSettlementSearchIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getSettlementSearch(),
          arrayContainingInAnyOrder("4030481-4", "NORM-1a75b353-14be-3c19-b22c-5c5334d78c8f", "Kiel"));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionRepositorySearchIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getRepositorySearch(),
          arrayContainingInAnyOrder("36197-5", "NORM-9caa05ee-be18-3003-bfa9-454aa6349a9f",
              "Universitätsbibliothek Kiel", "Universität Kiel. Bibliothek",
              "Christian-Albrechts-Universität zu Kiel. Universitätsbibliothek Kiel",
              "Christian-Albrechts-Universität. Universitätsbibliothek", "Universität Kiel. Universitätsbibliothek",
              "Königliche Universitätsbibliothek", "Christian-Albrechts-Universität zu Kiel. Zentralbibliothek",
              "Christian-Albrecht-Universität zu Kiel. Universitätsbibliothek", "Universitätsbibliothek",
              "Kieler Universitätsbibliothek", "soz_30002611", "DE-8"));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionIdnoSearchIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getIdnoSearch(), equalTo("Cod. ms. Bord. 1"));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionFulltextSearchIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getFulltextSearch(), equalTo(Fixtures.HSP_DESCRIPTION_FULLTEXT));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionAuthorSearchIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getAuthorSearch(),
          arrayContainingInAnyOrder("Katrin Sturm", "Konstantin Görlitz", "NORM-1f0e3dad-9990-3345-b743-9f8ffabdffc4",
              "NORM-invalide-id"));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionPublishYearSearchIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getPublishYearSearch(), equalTo(2020));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionDescribedObjectSearchIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getDescribedObjectSearch(), equalTo(Boolean.TRUE));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionDigitizedObjectSearchIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getDigitizedObjectSearch(), equalTo(Boolean.TRUE));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionMsIdentifierSearchIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getMsIdentifierSearch(),
          is("Kiel, Universitätsbibliothek Kiel, Cod. ms. Bord. 1"));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionMsIdentifierSortIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getMsIdentifierSort(),
          is("Kiel, Universitätsbibliothek Kiel, Kiel_UB_Cod-Ms-Bord-001"));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionPersonAuthorSearchIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getPersonAuthorSearch(), containsElements("Georg Laubmann", "Laubmann, Georg"));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionPersonBookbinderSearchIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getPersonBookbinderSearch(),
          containsElements("Christian Hannick", "Hannick, Christian"));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionPersonCommissionedBySearchIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getPersonCommissionedBySearch(),
          containsElements("Otto Kresten", "Kresten, Otto"));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionPersonIlluminatorSearchIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getPersonIlluminatorSearch(), containsElements("Wilhelm Meyer", "Meyer, Wilhelm"));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionPersonMentionedInSearchIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getPersonMentionedInSearch(), containsElements("Georg Thomas", "Thomas, Georg"));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionPersonOtherSearchIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getPersonOtherSearch(), containsElements("Friedrich Keinz", "Keinz, Friedrich"));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionPersonPreviousOwnerSearchIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getPersonPreviousOwnerSearch(),
          containsElements("Wolfgang Lackner", "Lackner, Wolfgang", "Brigitte Gullath", "Gullath, Brigitte"));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionPersonConservatorSearchIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getPersonConservatorSearch(),
          containsElements("Johann Conrad Irmischer", "Irmischer, Johann Conrad"));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionPersonScribeSearchIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getPersonScribeSearch(), containsElements("Eduard Ippel", "Ippel, Eduard"));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionPersonTranslatorSearchIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getPersonTranslatorSearch(),
          containsElements("Ingeborg Krekler", "Krekler, Ingeborg"));
    }

    @Test
    void givenASM_whenMapping_thenHspDescriptionLanguageFacetIsCorrect() throws Exception {
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      final HspDescription capturedDescription = descriptionCaptor.getValue().getFirst();

      assertThat(capturedDescription.getLanguageFacet(),
          arrayContainingInAnyOrder("NORM-c9089f3c-9ada-3018-af6f-fb1ee8d6501c",
              "NORM-5f02f088-9301-3d7b-a1ac-972c11bf3e7d", "unknown"));
    }

    @Test
    void givenASM_whenMapping_thenHspDigitizedIdIsCorrect() throws Exception {
      verify(mockedDigitizedService).saveAll(digitizedCaptor.capture());
      final HspDigitized capturedDigitzed = digitizedCaptor.getValue().getFirst();

      assertThat(capturedDigitzed.getId(), equalTo("2b51ebe7-f3eb-41ab-a50a-90da683240e6"));
    }

    @Test
    void givenASM_whenMapping_thenHspDigitizedDigitizationDateDisplayIsCorrect() throws Exception {
      final Date digitalizedDate = new SimpleDateFormat("yyyy-MM-dd").parse("2020-10-06");

      verify(mockedDigitizedService).saveAll(digitizedCaptor.capture());
      final HspDigitized capturedDigitized = digitizedCaptor.getValue().getFirst();

      assertThat(capturedDigitized.getDigitizationDateDisplay(), is(digitalizedDate));
    }

    @Test
    void givenASM_whenMapping_thenHspDigitizedDigitizationSettlementDisplayIsCorrect() throws Exception {
      verify(mockedDigitizedService).saveAll(digitizedCaptor.capture());
      final HspDigitized capturedDigitized = digitizedCaptor.getValue().getFirst();

      assertThat(capturedDigitized.getDigitizationSettlementDisplay(), is("Leipzig"));
    }

    @Test
    void givenASM_whenMapping_thenHspDigitizedDigitizationInstitutionDisplayIsCorrect() throws Exception {
      verify(mockedDigitizedService).saveAll(digitizedCaptor.capture());
      final HspDigitized capturedDigitized = digitizedCaptor.getValue().getFirst();

      assertThat(capturedDigitized.getDigitizationInstitutionDisplay(), is("Universitätsbibliothek Leipzig"));
    }

    @Test
    void givenASM_whenMapping_thenHspDigitizedIssuingDateDisplayIsCorrect() throws Exception {
      final Date issuedDate = new SimpleDateFormat("yyyy-MM-dd").parse("2022-12-02");

      verify(mockedDigitizedService).saveAll(digitizedCaptor.capture());
      final HspDigitized capturedDigitized = digitizedCaptor.getValue().getFirst();

      assertThat(capturedDigitized.getIssuingDateDisplay(), is(issuedDate));
    }

    @Test
    void givenASM_whenMapping_thenHspDigitizedThumbnailURIDisplayIsCorrect() throws Exception {
      verify(mockedDigitizedService).saveAll(digitizedCaptor.capture());
      final HspDigitized capturedDigitized = digitizedCaptor.getValue().getFirst();

      assertThat(capturedDigitized.getThumbnailURLDisplay(), is("http://thumbnail.uri"));
    }

    @Test
    void givenASM_whenMapping_thenManifestURISearchIsCorrect() throws Exception {
      verify(mockedDigitizedService).saveAll(digitizedCaptor.capture());
      final HspDigitized capturedDigitized = digitizedCaptor.getValue().getFirst();

      assertThat(capturedDigitized.getManifestURLSearch(),
          is("https://iiif.ub.uni-leipzig.de/0000029238/manifest.json"));
    }

    @Test
    void givenASM_whenMapping_thenGroupIdSearchIsCorrect() throws Exception {
      verify(mockedDigitizedService).saveAll(digitizedCaptor.capture());
      final HspDigitized capturedDigitized = digitizedCaptor.getValue().getFirst();

      assertThat(capturedDigitized.getGroupIdSearch(), is("4de2ec4a-09e0-11ee-be56-0242ac120002"));
    }
  }

  @Test
  void objectMapping_hasDigitizedFacet_WithoutDigitized() throws Exception {
    final Resource kodRes = resourceLoader.getResource("fixtures/loremIpsum_kod_digitalisat_ohne.xml");
    final Resource descRes = resourceLoader.getResource("fixtures/loremIpsum_beschreibung.xml");
    final ActivityStreamMessage asm = ActivityMessageHelper.fromResource(kodRes, descRes);

    messageHandler.handleMessage(asm);
    verify(mockedHspObjService).save(objectCaptor.capture());
    verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());

    assertThat(objectCaptor.getValue(), notNullValue());
    assertThat(objectCaptor.getValue().getDigitizedObjectSearch(), equalTo(Boolean.FALSE));

    assertThat(descriptionCaptor.getValue(), notNullValue());
    assertThat(descriptionCaptor.getValue(), hasSize(1));
    assertThat(descriptionCaptor.getValue().getFirst().getDigitizedObjectSearch(), equalTo(Boolean.FALSE));
  }

  @Test
  void whenKodContainsDigitizationWithExternalUrl_ThenIIIFFacetIsFalse() throws Exception {
    final Resource kodRes = resourceLoader.getResource("fixtures/loremIpsum_kod_digitalisat_extern.xml");
    final Resource descRes = resourceLoader.getResource("fixtures/loremIpsum_beschreibung.xml");
    final ActivityStreamMessage asm = ActivityMessageHelper.fromResource(kodRes, descRes);

    messageHandler.handleMessage(asm);
    verify(mockedHspObjService).save(objectCaptor.capture());
    verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());

    assertThat(objectCaptor.getValue(), notNullValue());
    assertThat(objectCaptor.getValue().getDigitizedIiifObjectSearch(), equalTo(Boolean.FALSE));

    assertThat(descriptionCaptor.getValue(), notNullValue());
    assertThat(descriptionCaptor.getValue(), hasSize(1));
    assertThat(descriptionCaptor.getValue().getFirst().getDigitizedIiifObjectSearch(), equalTo(Boolean.FALSE));
  }

  @Test
  void whenKodContainsDigitizationWithInternalUrl_ThenIIIFFacetIsTrue() throws Exception {
    final Resource kodRes = resourceLoader.getResource("fixtures/loremIpsum_kod_digitalisat_iiif.xml");
    final Resource descRes = resourceLoader.getResource("fixtures/loremIpsum_beschreibung.xml");
    final ActivityStreamMessage asm = ActivityMessageHelper.fromResource(kodRes, descRes);

    messageHandler.handleMessage(asm);
    verify(mockedHspObjService).save(objectCaptor.capture());
    verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());

    assertThat(objectCaptor.getValue(), notNullValue());
    assertThat(objectCaptor.getValue().getDigitizedIiifObjectSearch(), equalTo(Boolean.TRUE));

    assertThat(descriptionCaptor.getValue(), notNullValue());
    assertThat(descriptionCaptor.getValue(), hasSize(1));
    assertThat(descriptionCaptor.getValue().getFirst().getDigitizedIiifObjectSearch(), equalTo(Boolean.TRUE));
  }

  @Test
  void givenASMWithCatalogue_whenMapping_thenCatalogueIsSaved() throws Exception {
    final Resource catalogueRes = resourceLoader.getResource("fixtures/loremIpsum_catalog.xml");
    final ActivityStreamMessage asm = ActivityMessageHelper.fromResource(catalogueRes,
        ActivityStreamsDokumentTyp.KATALOG);

    messageHandler.handleMessage(asm);

    verify(mockedCatalogueService).save(catalogueCaptor.capture());
    final HspCatalog catalogue = catalogueCaptor.getValue();
    assertThat(catalogue, notNullValue());

    assertThat(catalogue.getId(), is("HSP-013a006f-03db-3539-aeff-eb8f18fda755"));
    assertThat(catalogue.getFullTextSearch(),
        is("DIE HANDSCHRIFTEN DES GROSSHERZOGTUMS LUXEMBURG herausgegeben von der Bibliotheque nationale de Luxembourg Band 2"));
  }

  @Test
  void givenASMWithKODAndDescription_whenMapping_thenAuthorityFileFacetIsCorrect() throws Exception {
    final Resource kodRes = resourceLoader.getResource("fixtures/loremIpsum_kod.xml");
    final Resource descRes = resourceLoader.getResource("fixtures/loremIpsum_beschreibung.xml");
    final ActivityStreamMessage asm = ActivityMessageHelper.fromResource(kodRes, descRes);

    messageHandler.handleMessage(asm);

    verify(mockedHspObjService).save(objectCaptor.capture());
    verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
    assertThat(objectCaptor.getValue().getAuthorityFileFacet(), arrayContainingInAnyOrder(
        "NORM-1a75b353-14be-3c19-b22c-5c5334d78c8f",
        "NORM-9caa05ee-be18-3003-bfa9-454aa6349a9f",
        "NORM-26cf9267-82fe-3bf1-a37a-c9960658499f",
        "NORM-654a4abc-3191-3e68-995b-4fdbd157cf9d",
        "NORM-ee1611b6-1f56-38e7-8c12-b40684dbb395",
        "NORM-1f0e3dad-9990-3345-b743-9f8ffabdffc4",
        "NORM-invalide-id",
        "NORM-87370ae5-cf7c-3f3e-8adc-8dad1f9d2455",
        "NORM-258c6031-1128-325c-82ee-3859e6930fe1",
        "NORM-1afa34a7-f984-3eab-9bb0-a7d494132ee5",
        "NORM-82161242-827b-303e-aacf-9c726942a1e4",
        "NORM-38af8613-4b65-30f1-8fe3-3d30dd76442e",
        "NORM-65ded535-3c5e-348d-8b7d-48c591b8f430",
        "NORM-02522a2b-2726-3b0a-83bb-19f2d8d9524d",
        "NORM-9fc3d715-2ba9-336a-a70e-36d0ed79bc43",
        "NORM-96da2f59-0cd7-346b-bde0-051047b0d6f7",
        "NORM-7f1de29e-6da1-3d22-b51c-68001e7e0e54",
        "NORM-8f53295a-7387-3494-a9bc-8dd6c3c7104f",
        "NORM-8f855179-6779-3eee-b66c-225f7883bdcb",
        "NORM-045117b0-e0a1-3a24-ab97-65e79cbf113f"));

    assertThat(objectCaptor.getValue().getAuthorityFileFacet(),
        arrayContainingInAnyOrder(descriptionCaptor.getValue().getFirst().getAuthorityFileFacet()));
  }

  @Test
  void testHandleMessage_GivenAnyAndRemoveALL() throws ActivityStreamsException, PersistenceServiceException {

    ActivityStreamObject activityStreamObject = ActivityStreamObject.builder().withType(ActivityStreamsDokumentTyp.ANY)
        .build();

    final ActivityStreamMessage asm = (ActivityStreamMessage) ActivityStreamMessage.builder()
        .addObject(activityStreamObject)
        .withType(ActivityStreamAction.REMOVE_ALL).build();

    messageHandler.handleMessage(asm);

    verify(mockedHspObjService, (times(1))).deleteAll();
    verify(mockedCatalogueService, (times(1))).deleteAll();
  }
}