package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten.NormdatenService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.SolrMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspDigitized;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspObject;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.RepositoryImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.EntitiyService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.impl.EntityServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.impl.HspObjectGroupServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.ActivityMessageHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.fixture.Fixtures;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.fixture.NormdatenFixture;
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
  static NormdatenService normdatenService;
  static SolrMapper solrMapper;
  static ObjectMapper objectMapper;
  private final ResourceLoader resourceLoader;
  SolrKafkaMessageHandler messageHandler;
  EntitiyService<HspObject> mockedHspObjService;
  EntitiyService<HspDescription> mockedDescriptionService;
  EntitiyService<HspDigitized> mockedDigiService;
  @Mock
  RepositoryImpl<HspObject> mockedHspObjRepo;
  @Mock
  RepositoryImpl<HspDescription> mockedHspDescRepo;
  @Mock
  RepositoryImpl<HspDigitized> mockedHspDigitizedRepo;
  @Captor
  private ArgumentCaptor<HspObject> objectCaptor;
  @Captor
  private ArgumentCaptor<ArrayList<HspDigitized>> digitizedCaptor;
  @Captor
  private ArgumentCaptor<ArrayList<HspDescription>> descriptionCaptor;

  public SolrKafkaMessageHandlerTest(
          @Autowired final ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @BeforeAll
  public static void setup() {
    solrMapper = new SolrMapper();
    objectMapper = ObjectMapperFactory.getObjectMapper();
  }

  @BeforeEach
  public void init() {
    mockedHspObjService = Mockito.spy(new EntityServiceImpl<>(mockedHspObjRepo));
    mockedDescriptionService = Mockito.spy(new EntityServiceImpl<>(mockedHspDescRepo));
    mockedDigiService = Mockito.spy(new EntityServiceImpl<>(mockedHspDigitizedRepo));

    final HspObjectGroupServiceImpl hspObjectGroupService = new HspObjectGroupServiceImpl(mockedHspObjService, mockedDescriptionService, mockedDigiService);

    messageHandler = new SolrKafkaMessageHandler(solrMapper, hspObjectGroupService);
    solrMapper.setNormDatenService(normdatenService);
  }

  @Test
  void whenMappingOnASMIsCalled_thenHSPObjectGroupIsCorrect() throws Exception {
    final Resource kodRes = resourceLoader.getResource("fixtures/loremIpsum_kod.xml");
    final Resource descRes = resourceLoader.getResource("fixtures/loremIpsum_beschreibung.xml");

    final ActivityStreamMessage asm = ActivityMessageHelper.getActivityStreamMessageFromResources(kodRes, descRes);

    Date publishingDate = new SimpleDateFormat("yyyy-MM-dd").parse("2020-01-01");

    NormdatenFixture.runWithNormdatenService(() -> {
      messageHandler.handleMessage(asm);

      verify(mockedHspObjService).save(objectCaptor.capture());
      verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());
      verify(mockedDigiService).saveAll(digitizedCaptor.capture());

      // hsp:Object assertions
      final HspObject capObj = objectCaptor.getValue();
      assertThat(capObj.getId(), equalTo("__UUID__"));
      assertThat(capObj.getGroupIdSearch(), equalTo("__UUID__"));
      assertThat(capObj.getTypeSearch(), equalTo("hsp:object"));
      assertThat(capObj.getIdnoSearch(), equalTo("Cod. ms. Bord. 1"));
      assertThat(capObj.getDescribedObjectFacet(), equalTo(Boolean.TRUE.toString()));
      assertThat(capObj.getDigitizedObjectFacet(), equalTo(Boolean.TRUE.toString()));
      assertThat(capObj.getPersistentURLDisplay(), is("https://resolver.staatsbibliothek-berlin.de/__UUID__"));
      //check the normdatum enriched data
      assertThat(capObj.getSettlementDisplay(), is("Kiel"));
      assertThat(capObj.getSettlementFacet(), arrayContainingInAnyOrder("Kiel"));
      assertThat(capObj.getSettlementSearch(), arrayContainingInAnyOrder("4030481-4", "NORM-1a75b353-14be-3c19-b22c-5c5334d78c8f", "Kiel"));

      assertThat(capObj.getRepositoryDisplay(), is("Universitätsbibliothek Kiel"));
      assertThat(capObj.getRepositoryFacet(), arrayContainingInAnyOrder("Universitätsbibliothek Kiel"));
      assertThat(capObj.getRepositorySearch(), arrayContainingInAnyOrder("36197-5", "NORM-9caa05ee-be18-3003-bfa9-454aa6349a9f", "Universitätsbibliothek Kiel", "Universität Kiel. Bibliothek", "Christian-Albrechts-Universität zu Kiel. Universitätsbibliothek Kiel", "Christian-Albrechts-Universität. Universitätsbibliothek", "Universität Kiel. Universitätsbibliothek", "Königliche Universitätsbibliothek", "Christian-Albrechts-Universität zu Kiel. Zentralbibliothek", "Christian-Albrecht-Universität zu Kiel. Universitätsbibliothek", "Universitätsbibliothek", "Kieler Universitätsbibliothek", "soz_30002611", "DE-8"));

      assertThat(capObj.getMsIdentifierSearch(), is("Kiel, Universitätsbibliothek Kiel, Cod. ms. Bord. 1"));
      assertThat(capObj.getMsIdentifierSort(), is("Kiel, Universitätsbibliothek Kiel, 02f-0001-0005-0001"));

      // hsp:description assertions
      assertThat(descriptionCaptor.getValue().size(), equalTo(1));
      final HspDescription capturedDescription = descriptionCaptor.getValue().get(0);

      assertThat(capturedDescription.getId(), equalTo("__UUID__"));
      assertThat(capturedDescription.getGroupIdSearch(), equalTo("__UUID__"));
      assertThat(capturedDescription.getTypeSearch(), equalTo("hsp:description"));
      assertThat(capturedDescription.getSettlementSearch(), arrayContainingInAnyOrder("4030481-4", "NORM-1a75b353-14be-3c19-b22c-5c5334d78c8f", "Kiel"));
      assertThat(capturedDescription.getRepositorySearch(), arrayContainingInAnyOrder("36197-5", "NORM-9caa05ee-be18-3003-bfa9-454aa6349a9f", "Universitätsbibliothek Kiel", "Universität Kiel. Bibliothek", "Christian-Albrechts-Universität zu Kiel. Universitätsbibliothek Kiel", "Christian-Albrechts-Universität. Universitätsbibliothek", "Universität Kiel. Universitätsbibliothek", "Königliche Universitätsbibliothek", "Christian-Albrechts-Universität zu Kiel. Zentralbibliothek", "Christian-Albrecht-Universität zu Kiel. Universitätsbibliothek", "Universitätsbibliothek", "Kieler Universitätsbibliothek", "soz_30002611", "DE-8"));
      assertThat(capturedDescription.getIdnoSearch(), equalTo("Cod. ms. Bord. 1"));
      assertThat(capturedDescription.getFulltextSearch(), equalTo(Fixtures.HSP_DESCRIPTION_FULLTEXT));
      assertThat(capturedDescription.getDescAuthorSearch(), arrayContainingInAnyOrder("Katrin Sturm", "Konstantin Görlitz"));
      assertThat(capturedDescription.getDescPublishDateSearch(), equalTo(publishingDate));
      assertThat(capturedDescription.getDescribedObjectFacet(), equalTo(Boolean.TRUE.toString()));
      assertThat(capturedDescription.getDigitizedObjectFacet(), equalTo(Boolean.TRUE.toString()));
      assertThat(capturedDescription.getMsIdentifierSearch(), is("Kiel, Universitätsbibliothek Kiel, Cod. ms. Bord. 1"));
      assertThat(capturedDescription.getMsIdentifierSort(), is("Kiel, Universitätsbibliothek Kiel, 02f-0001-0005-0001"));

      // hsp:digitized assertions
      assertThat(digitizedCaptor.getValue().size(), equalTo(1));
      final HspDigitized capturedDigitalCopy = digitizedCaptor.getValue().get(0);
      final Date digitalizedDate = new SimpleDateFormat("yyyy-MM-dd").parse("2020-10-06");
      final Date issuedDate = new SimpleDateFormat("yyyy-MM-dd").parse("2020-10-23");

      assertThat(capturedDigitalCopy.getId(), equalTo("d2dee089-2f5d-4247-b2fb-ae5963ad5480"));
      assertThat(capturedDigitalCopy.getDigitizationDateDisplay(), is(digitalizedDate));
      assertThat(capturedDigitalCopy.getDigitizationPlaceDisplay(), is("Leipzig"));
      assertThat(capturedDigitalCopy.getDigitizationOrganizationDisplay(), is("Universitätsbibliothek Leipzig"));
      assertThat(capturedDigitalCopy.getIssuingDateDisplay(), is(issuedDate));
      assertThat(capturedDigitalCopy.getThumbnailURIDisplay(), is("http://thumbnail.uri"));
      assertThat(capturedDigitalCopy.getManifestURIDisplay(), is("https://iiif.ub.uni-leipzig.de/0000029238/manifest.json"));
      assertThat(capturedDigitalCopy.getSubTypeDisplay(), is("komplettVomOriginal"));
      assertThat(capturedDigitalCopy.getGroupIdSearch(), is("__UUID__"));
    }, solrMapper);
  }

  @Test
  void objectMapping_hasDigitizedFacet_WithoutDigitized() throws Exception {
    final Resource kodRes = resourceLoader.getResource("fixtures/loremIpsum_kod_modifiziert.xml");
    final Resource descRes = resourceLoader.getResource("fixtures/loremIpsum_beschreibung.xml");
    final ActivityStreamMessage asm = ActivityMessageHelper.getActivityStreamMessageFromResources(kodRes, descRes);

    messageHandler.handleMessage(asm);
    verify(mockedHspObjService).save(objectCaptor.capture());
    verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());

    assertThat(objectCaptor.getValue(), notNullValue());
    assertThat(objectCaptor.getValue()
            .getDigitizedObjectFacet(), equalTo(Boolean.FALSE.toString()));

    assertThat(descriptionCaptor.getValue(), notNullValue());
    assertThat(descriptionCaptor.getValue(), hasSize(1));
    assertThat(descriptionCaptor.getValue()
            .get(0)
            .getDigitizedObjectFacet(), equalTo(Boolean.FALSE.toString()));
  }

  @Test
  void whenKodContainsDigitizationWithExternalUrl_ThenIiifFacetIsFalse() throws Exception {
    final Resource kodRes = resourceLoader.getResource("fixtures/loremIpsum_kod_modifiziert_02.xml");
    final Resource descRes = resourceLoader.getResource("fixtures/loremIpsum_beschreibung.xml");
    final ActivityStreamMessage asm = ActivityMessageHelper.getActivityStreamMessageFromResources(kodRes, descRes);

    messageHandler.handleMessage(asm);
    verify(mockedHspObjService).save(objectCaptor.capture());
    verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());

    assertThat(objectCaptor.getValue(), notNullValue());
    assertThat(objectCaptor.getValue()
            .getDigitizedIiifObjectFacet(), equalTo(Boolean.FALSE.toString()));

    assertThat(descriptionCaptor.getValue(), notNullValue());
    assertThat(descriptionCaptor.getValue(), hasSize(1));
    assertThat(descriptionCaptor.getValue()
            .get(0)
            .getDigitizedIiifObjectFacet(), equalTo(Boolean.FALSE.toString()));
  }

  @Test
  void whenKodContainsDigitizationWithInternalUrl_ThenIiifFacetIsTrue() throws Exception {
    final Resource kodRes = resourceLoader.getResource("fixtures/loremIpsum_kod.xml");
    final Resource descRes = resourceLoader.getResource("fixtures/loremIpsum_beschreibung.xml");
    final ActivityStreamMessage asm = ActivityMessageHelper.getActivityStreamMessageFromResources(kodRes, descRes);

    messageHandler.handleMessage(asm);
    verify(mockedHspObjService).save(objectCaptor.capture());
    verify(mockedDescriptionService).saveAll(descriptionCaptor.capture());

    assertThat(objectCaptor.getValue(), notNullValue());
    assertThat(objectCaptor.getValue()
            .getDigitizedIiifObjectFacet(), equalTo(Boolean.TRUE.toString()));

    assertThat(descriptionCaptor.getValue(), notNullValue());
    assertThat(descriptionCaptor.getValue(), hasSize(1));
    assertThat(descriptionCaptor.getValue()
            .get(0)
            .getDigitizedIiifObjectFacet(), equalTo(Boolean.TRUE.toString()));
  }
}
