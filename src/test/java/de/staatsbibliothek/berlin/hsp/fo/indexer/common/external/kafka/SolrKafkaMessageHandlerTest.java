package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.AuthorityFileService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.SolrMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspCatalog;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspDigitized;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspObject;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.PersistenceServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.RepositoryImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.entity.impl.HspCatalogService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.entity.impl.HspObjectGroupService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.replication.ReplicationAdminService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.ActivityMessageHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.fixture.Fixtures;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.fixture.AuthorityFileFixture;
import de.staatsbibliothek.berlin.hsp.fo.indexer.type.HspObjectType;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.mapper.ObjectMapperFactory;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.model.ActivityStreamMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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

import javax.annotation.concurrent.NotThreadSafe;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.matcher.ArrayContainsElementsMatcher.containsElements;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.ArrayMatching.arrayContainingInAnyOrder;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.verify;

@ActiveProfiles("integration")
@ExtendWith(SpringExtension.class)
@NotThreadSafe
class SolrKafkaMessageHandlerTest {
  @Mock
  static AuthorityFileService authorityFileService;
  static SolrMapper solrMapper;
  static ObjectMapper objectMapper;
  private final ResourceLoader resourceLoader;
  private SolrKafkaMessageHandler messageHandler;

  private RepositoryImpl<HspObject> mockedHspObjService;
  private RepositoryImpl<HspDescription> mockedDescriptionService;
  private RepositoryImpl<HspDigitized> mockedDigitizedService;
  private RepositoryImpl<HspCatalog> mockedCatalogueService;

  @Mock
  ReplicationAdminService mockedReplicationAdminService;
  @Mock
  PersistenceServiceImpl<HspObject> mockedHspObjRepo;
  @Mock
  PersistenceServiceImpl<HspDescription> mockedHspDescRepo;
  @Mock
  PersistenceServiceImpl<HspDigitized> mockedHspDigitizedRepo;
  @Mock
  PersistenceServiceImpl<HspCatalog> mockedCatalogueRepo;

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
  public static void setup() {
    solrMapper = new SolrMapper();
    objectMapper = ObjectMapperFactory.getObjectMapper();
  }

  @BeforeEach
  public void init() {
    mockedHspObjService = Mockito.spy(new RepositoryImpl<>(mockedHspObjRepo, HspObjectType.OBJECT));
    mockedDescriptionService = Mockito.spy(new RepositoryImpl<>(mockedHspDescRepo, HspObjectType.DESCRIPTION));
    mockedDigitizedService = Mockito.spy(new RepositoryImpl<>(mockedHspDigitizedRepo, HspObjectType.DIGITIZATION));
    mockedCatalogueService = Mockito.spy(new RepositoryImpl<>(mockedCatalogueRepo, HspObjectType.CATALOG));

    final HspObjectGroupService hspObjectGroupService = new HspObjectGroupService(mockedHspObjService, mockedDescriptionService, mockedDigitizedService);
    final HspCatalogService hspCatalogService = new HspCatalogService(mockedCatalogueService);

    messageHandler = new SolrKafkaMessageHandler(solrMapper, hspObjectGroupService, hspCatalogService, mockedReplicationAdminService);
    solrMapper.setAuthorityFileService(authorityFileService);
  }

  @Test
  void whenMappingOnASMIsCalled_thenHSPObjectGroupIsCorrect() throws Exception {
    final Resource kodRes = resourceLoader.getResource("fixtures/loremIpsum_kod_digitalisat_iiif.xml");
    final Resource descRes = resourceLoader.getResource("fixtures/loremIpsum_beschreibung.xml");

    final ActivityStreamMessage asm = ActivityMessageHelper.fromResource(kodRes, descRes);

    Integer publishingYear = 2020;

    AuthorityFileFixture.runWithAuthorityFileService(() -> {
      messageHandler.handleMessage(asm);

      verify(mockedHspObjService).save(objectCaptor.capture());
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      verify(mockedDigitizedService).saveAll(digitizedCaptor.capture());

      // hsp:Object assertions
      final HspObject capObj = objectCaptor.getValue();
      assertThat(capObj.getId(), equalTo("4de2ec4a-09e0-11ee-be56-0242ac120002"));
      assertThat(capObj.getGroupIdSearch(), equalTo("4de2ec4a-09e0-11ee-be56-0242ac120002"));
      assertThat(capObj.getTypeSearch(), equalTo("hsp:object"));
      assertThat(capObj.getIdnoSearch(), equalTo("Cod. ms. Bord. 1"));
      assertThat(capObj.getDescribedObjectSearch(), equalTo(Boolean.TRUE));
      assertThat(capObj.getDigitizedObjectSearch(), equalTo(Boolean.TRUE));
      assertThat(capObj.getPersistentURLDisplay(), is("https://resolver.staatsbibliothek-berlin.de/__UUID__"));
      //check the authority file enriched data
      assertThat(capObj.getSettlementDisplay(), is("Kiel"));
      assertThat(capObj.getSettlementFacet(), arrayContainingInAnyOrder("Kiel"));
      assertThat(capObj.getSettlementSearch(), arrayContainingInAnyOrder("4030481-4", "NORM-1a75b353-14be-3c19-b22c-5c5334d78c8f", "Kiel"));
      assertThat(capObj.getSettlementAuthorityFileDisplay(), arrayContainingInAnyOrder("NORM-1a75b353-14be-3c19-b22c-5c5334d78c8f"));

      assertThat(capObj.getRepositoryDisplay(), is("Universitätsbibliothek Kiel"));
      assertThat(capObj.getRepositoryFacet(), arrayContainingInAnyOrder("Universitätsbibliothek Kiel"));
      assertThat(capObj.getRepositorySearch(), arrayContainingInAnyOrder("36197-5", "NORM-9caa05ee-be18-3003-bfa9-454aa6349a9f", "Universitätsbibliothek Kiel", "Universität Kiel. Bibliothek", "Christian-Albrechts-Universität zu Kiel. Universitätsbibliothek Kiel", "Christian-Albrechts-Universität. Universitätsbibliothek", "Universität Kiel. Universitätsbibliothek", "Königliche Universitätsbibliothek", "Christian-Albrechts-Universität zu Kiel. Zentralbibliothek", "Christian-Albrecht-Universität zu Kiel. Universitätsbibliothek", "Universitätsbibliothek", "Kieler Universitätsbibliothek", "soz_30002611", "DE-8"));
      assertThat(capObj.getRepositoryAuthorityFileDisplay(), arrayContainingInAnyOrder("NORM-9caa05ee-be18-3003-bfa9-454aa6349a9f"));

      assertThat(capObj.getMsIdentifierSearch(), is("Kiel, Universitätsbibliothek Kiel, Cod. ms. Bord. 1"));
      assertThat(capObj.getMsIdentifierSort(), is("Kiel, Universitätsbibliothek Kiel, Kiel_UB_Cod-Ms-Bord-001"));
      assertThat(capObj.getOrigPlaceAuthorityFileDisplay(), arrayContainingInAnyOrder("NORM-ee1611b6-1f56-38e7-8c12-b40684dbb395"));
      // hsp:description assertions
      assertThat(descriptionCaptor.getValue().size(), equalTo(1));
      final HspDescription capturedDescription = descriptionCaptor.getValue().get(0);

      assertThat(capturedDescription.getId(), equalTo("__UUID__"));
      assertThat(capturedDescription.getGroupIdSearch(), equalTo("4de2ec4a-09e0-11ee-be56-0242ac120002"));
      assertThat(capturedDescription.getTypeSearch(), equalTo("hsp:description"));
      assertThat(capturedDescription.getSettlementSearch(), arrayContainingInAnyOrder("4030481-4", "NORM-1a75b353-14be-3c19-b22c-5c5334d78c8f", "Kiel"));
      assertThat(capturedDescription.getRepositorySearch(), arrayContainingInAnyOrder("36197-5", "NORM-9caa05ee-be18-3003-bfa9-454aa6349a9f", "Universitätsbibliothek Kiel", "Universität Kiel. Bibliothek", "Christian-Albrechts-Universität zu Kiel. Universitätsbibliothek Kiel", "Christian-Albrechts-Universität. Universitätsbibliothek", "Universität Kiel. Universitätsbibliothek", "Königliche Universitätsbibliothek", "Christian-Albrechts-Universität zu Kiel. Zentralbibliothek", "Christian-Albrecht-Universität zu Kiel. Universitätsbibliothek", "Universitätsbibliothek", "Kieler Universitätsbibliothek", "soz_30002611", "DE-8"));
      assertThat(capturedDescription.getIdnoSearch(), equalTo("Cod. ms. Bord. 1"));
      assertThat(capturedDescription.getFulltextSearch(), equalTo(Fixtures.HSP_DESCRIPTION_FULLTEXT));
      assertThat(capturedDescription.getAuthorSearch(), arrayContainingInAnyOrder("Katrin Sturm", "Konstantin Görlitz", "NORM-1f0e3dad-9990-3345-b743-9f8ffabdffc4", "NORM-invalide-id"));
      assertThat(capturedDescription.getPublishYearSearch(), equalTo(publishingYear));
      assertThat(capturedDescription.getDescribedObjectSearch(), equalTo(Boolean.TRUE));
      assertThat(capturedDescription.getDigitizedObjectSearch(), equalTo(Boolean.TRUE));
      assertThat(capturedDescription.getMsIdentifierSearch(), is("Kiel, Universitätsbibliothek Kiel, Cod. ms. Bord. 1"));
      assertThat(capturedDescription.getMsIdentifierSort(), is("Kiel, Universitätsbibliothek Kiel, Kiel_UB_Cod-Ms-Bord-001"));
      assertThat(capturedDescription.getPersonAuthorSearch(), containsElements("Georg Laubmann", "Laubmann, Georg"));
      assertThat(capturedDescription.getPersonBookbinderSearch(), containsElements("Christian Hannick", "Hannick, Christian"));
      assertThat(capturedDescription.getPersonCommissionedBySearch(), containsElements("Otto Kresten", "Kresten, Otto"));
      assertThat(capturedDescription.getPersonIlluminatorSearch(), containsElements("Wilhelm Meyer", "Meyer, Wilhelm"));
      assertThat(capturedDescription.getPersonMentionedInSearch(), containsElements("Georg Thomas", "Thomas, Georg"));
      assertThat(capturedDescription.getPersonOtherSearch(), containsElements("Friedrich Keinz", "Keinz, Friedrich"));
      assertThat(capturedDescription.getPersonPreviousOwnerSearch(), containsElements("Wolfgang Lackner", "Lackner, Wolfgang", "Brigitte Gullath", "Gullath, Brigitte"));
      assertThat(capturedDescription.getPersonConservatorSearch(), containsElements("Johann Conrad Irmischer", "Irmischer, Johann Conrad"));
      assertThat(capturedDescription.getPersonScribeSearch(), containsElements("Eduard Ippel", "Ippel, Eduard"));
      assertThat(capturedDescription.getPersonTranslatorSearch(), containsElements("Ingeborg Krekler", "Krekler, Ingeborg"));
      // hsp:digitized assertions
      assertThat(digitizedCaptor.getValue().size(), equalTo(1));
      final HspDigitized capturedDigitalCopy = digitizedCaptor.getValue().get(0);
      final Date digitalizedDate = new SimpleDateFormat("yyyy-MM-dd").parse("2020-10-06");
      final Date issuedDate = new SimpleDateFormat("yyyy-MM-dd").parse("2022-12-02");

      assertThat(capturedDigitalCopy.getId(), equalTo("2b51ebe7-f3eb-41ab-a50a-90da683240e6"));
      assertThat(capturedDigitalCopy.getDigitizationDateDisplay(), is(digitalizedDate));
      assertThat(capturedDigitalCopy.getDigitizationSettlementDisplay(), is("Leipzig"));
      assertThat(capturedDigitalCopy.getDigitizationInstitutionDisplay(), is("Universitätsbibliothek Leipzig"));
      assertThat(capturedDigitalCopy.getIssuingDateDisplay(), is(issuedDate));
      assertThat(capturedDigitalCopy.getThumbnailURIDisplay(), is("http://thumbnail.uri"));
      assertThat(capturedDigitalCopy.getManifestURISearch(), is("https://iiif.ub.uni-leipzig.de/0000029238/manifest.json"));
      // de-activated until value is provided correctly
      // assertThat(capturedDigitalCopy.getSubTypeDisplay(), is("komplettVomOriginal"));
      assertThat(capturedDigitalCopy.getGroupIdSearch(), is("4de2ec4a-09e0-11ee-be56-0242ac120002"));
    }, solrMapper);
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
    assertThat(descriptionCaptor.getValue().get(0).getDigitizedObjectSearch(), equalTo(Boolean.FALSE));
  }

  @Test
  void whenKodContainsDigitizationWithExternalUrl_ThenIiifFacetIsFalse() throws Exception {
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
    assertThat(descriptionCaptor.getValue().get(0).getDigitizedIiifObjectSearch(), equalTo(Boolean.FALSE));
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
    assertThat(descriptionCaptor.getValue().get(0).getDigitizedIiifObjectSearch(), equalTo(Boolean.TRUE));
  }

  @Test
  void givenASMWithCatalogue_whenMapping_thenCatalogueIsSaved() throws Exception {
    final Resource catalogueRes = resourceLoader.getResource("fixtures/loremIpsum_catalog.xml");
    final ActivityStreamMessage asm = ActivityMessageHelper.fromResource(catalogueRes, ActivityStreamsDokumentTyp.KATALOG);

    messageHandler.handleMessage(asm);

    verify(mockedCatalogueService).save(catalogueCaptor.capture());
    final HspCatalog catalogue = catalogueCaptor.getValue();
    assertThat(catalogue, notNullValue());

    assertThat(catalogue.getId(), is("HSP-013a006f-03db-3539-aeff-eb8f18fda755"));
    assertThat(catalogue.getTitleSearch(), is("Titel des Katalogs"));
    assertThat(catalogue.getAuthorSearch(), arrayContainingInAnyOrder("Thomas Falmagne"));
    assertThat(catalogue.getFullTextSearch(), is("DIE HANDSCHRIFTEN DES GROSSHERZOGTUMS LUXEMBURG herausgegeben von der Bibliotheque nationale de Luxembourg Band 2"));
    assertThat(catalogue.getPublisherSearch(), arrayContainingInAnyOrder("Harrassowitz"));
    assertThat(catalogue.getEditorSearch(), arrayContainingInAnyOrder("Armin Dietzel", "Günther Bauer"));
    assertThat(catalogue.getPublishYearSearch(), is(2017));
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

      assertThat(objectCaptor.getValue().getAuthorityFileFacet(), arrayContainingInAnyOrder(descriptionCaptor.getValue().get(0).getAuthorityFileFacet()));
  }
}