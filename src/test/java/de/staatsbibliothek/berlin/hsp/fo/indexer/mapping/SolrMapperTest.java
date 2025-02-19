package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.util.FacetHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.util.MappingHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspCatalog;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspObject;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.ActivityMessageHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.fixture.AuthorityFileFixture;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.fixture.Fixtures;
import de.staatsbibliothek.berlin.hsp.fo.indexer.type.HspObjectType;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.mapper.ObjectMapperFactory;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.model.ActivityStreamMessage;
import org.dom4j.DocumentException;
import org.exparity.hamcrest.date.DateMatchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.fixture.FileFixture.dataFromResourceFilename;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.collection.ArrayMatching.arrayContainingInAnyOrder;
import static org.hamcrest.collection.ArrayMatching.hasItemInArray;
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

    asm_loremIpsum = ActivityMessageHelper.fromResource(kodRes, descRes, descRes2);
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

  private HspDescription mapDescriptionAndAssertNotEmpty(final String filename, final HspObject hspObject) throws IOException, DocumentException {
    final Optional<HspDescription> hspDescriptionOpt = solrMapper.mapHspDescription(dataFromResourceFilename(filename), hspObject, Boolean.TRUE, Boolean.TRUE);
    return getOptional(hspDescriptionOpt);
  }

  private HspObject mapHspObjectAndAssertNotEmpty(final String filename) throws IOException, DocumentException {
    final Optional<HspObject> hspObjectOpt = solrMapper.mapHspObject(dataFromResourceFilename(filename));
    return getOptional(hspObjectOpt);
  }

  private HspCatalog mapHspCatalog(final String filename) throws IOException {
    final Optional<HspCatalog> HspCatalogOpt = solrMapper.mapHspCatalog(dataFromResourceFilename(filename));
    return getOptional(HspCatalogOpt);
  }

  private <T> T getOptional(final Optional<T> opt) {
    assertThat(opt, isPresent());
    return opt.get();
  }

  @AfterEach
  void tearDown() {
    solrMapper.setAuthorityFileService(null);
  }


    @Test
    void whenHspObjectIsMapped_thenIdnoSortKeyIsMappedCorrectly() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getIdnoSortKey(), is("Kiel_UB_Cod-Ms-Bord-001"));
    }



  @Nested
  @DisplayName("Retro Description Mapping")
  class RetroDescription {
    @Test
    void whenRetroDescriptionIsMapped_thenAllAttributesAreMappedCorrectly() throws Exception {
/*      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", new HspObject());

      assertThat(hspDescription.getCatalogIIIFManifestRangeUrlDisplay(), is("https://iiif.ub.uni-leipzig.de/0000034537/range/LOG_0014"));
      assertThat(hspDescription.getCatalogIIIFManifestUrlDisplay(), is("https://iiif.ub.uni-leipzig.de/0000034537/manifest.json"));
      assertThat(hspDescription.getCatalogIdDisplay(), is("HSP-a8abb4bb-284b-3b27-aa7c-b790dc20f80b"));

      assertThat(hspDescription.getFulltextSearch(), containsString(Fixtures.RETRO_DESCRIPTION_MSPART_OTHER));*/
    }
  }

  @Nested
  @DisplayName("Description Mapping")
  class Description {
    @Test
    @DisplayName("accompanying-material-search field")
    void whenDescriptionIsMapped_thenAccompanyingMaterialSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getAccompanyingMaterialSearch(), is(Fixtures.LOREM_IPSUM_DESCRIPTION_ACCOMPANYING_MATERIAL));
    }

    @Test
    @DisplayName("binding-orig-place-search field")
    void whenDescriptionIsMapped_thenBindingOrigPlaceSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getBindingOrigPlaceSearch(), arrayContainingInAnyOrder("Konstanz, Bodenseeraum", "4007405-5", "4264875-5"));
    }

    @Test
    @DisplayName("binding-search field")
    void whenDescriptionIsMapped_thenBindingSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getBindingSearch(), is(Fixtures.LOREM_IPSUM_DESCRIPTION_BINDING));
    }

    @Test
    @DisplayName("booklet-search field")
    void whenDescriptionIsMapped_thenBookletSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getBookletSearch(), is(Fixtures.LOREM_IPSUM_DESCRIPTION_BOOKLET));
    }

    @Test
    @DisplayName("collection-search field")
    void whenDescriptionIsMapped_thenCollectionSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getCollectionSearch(), is("Aktuelle Sammlung"));
    }

    @Test
    @DisplayName("decoration-search field")
    void whenDescriptionIsMapped_thenDecorationSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getDecorationSearch(), is(Fixtures.LOREM_IPSUM_DESCRIPTION_DECORATION));
    }

    @Test
    @DisplayName("depth-facet field")
    void whenDescriptionIsMapped_thenDepthFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getDepthFacet(), arrayContainingInAnyOrder(2F));
    }

    @Test
    @DisplayName("description-status-facet field")
    void whenDescriptionIsMapped_thenDescriptionStatusIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getDescStatusFacet(), is("intern"));
    }

    @Test
    @DisplayName("dimension-display field")
    void whenDescriptionIsMapped_thenDimensionsDisplayIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getDimensionsDisplay(), arrayContainingInAnyOrder("16 × 12 (Teil I)", "12,5 × 15,5 (Teil II)"));
    }

    @Test
    @DisplayName("explicit-search field")
    void whenDescriptionIsMapped_thenExplicitSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getExplicitSearch(), arrayContainingInAnyOrder("sed libera nos a malo.", "et accusam et justo duo"));
    }

    @Test
    @DisplayName("format-facet field")
    void whenDescriptionIsMapped_thenFormatFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getFormatFacet(), arrayContaining("folio", "oblong"));
    }

    @Test
    @DisplayName("format-search field")
    void whenDescriptionIsMapped_thenFormatSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getFormatSearch(), arrayContainingInAnyOrder("folio", "oblong"));
    }

    @Test
    @DisplayName("format-type-display field")
    void whenDescriptionIsMapped_thenFormatTypeDisplayIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getFormatTypeDisplay(), arrayContainingInAnyOrder("deduced", "factual"));
    }

    @Test
    @DisplayName("format-type-facet field")
    void whenDescriptionIsMapped_thenFormatTypeFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getFormatTypeFacet(), arrayContainingInAnyOrder("deduced", "factual"));
    }

    @Test
    @DisplayName("fragment-search field")
    void whenDescriptionIsMapped_thenFragmentSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getFragmentSearch(), is(Fixtures.LOREM_IPSUM_DESCRIPTION_FRAGMENT));
    }

    @Test
    @DisplayName("fulltext-search field")
    void whenDescriptionIsMapped_thenFulltextSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getFulltextSearch(), not(containsString("Mischung aus Papier und Leinen")));
    }

    @Test
    @DisplayName("has-notation-search field")
    void whenDescriptionIsMapped_thenHasNotationSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getHasNotationSearch(), is("no"));
    }

    @Test
    @DisplayName("has-notation-facet field")
    void whenDescriptionIsMapped_thenHasNotationFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getHasNotationFacet(), arrayContainingInAnyOrder("no"));
    }

    @Test
    @DisplayName("height-facet field")
    void whenDescriptionIsMapped_thenHeightFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getHeightFacet(), arrayContainingInAnyOrder(16F, 12.5F));
    }

    @Test
    @DisplayName("history-search field")
    void whenDescriptionIsMapped_thenHistorySearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getHistorySearch(), is(Fixtures.LOREM_IPSUM_DESCRIPTION_HISTORY));
    }

    @Test
    @DisplayName("id field")
    void whenDescriptionIsMapped_thenIdIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getId(), is("__UUID__"));
    }

    @Test
    @DisplayName("idno-alternative-search field")
    void whenDescriptionIsMapped_thenIdnoAltSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getIdnoAltSearch(), arrayContainingInAnyOrder("HSP-3d18a39c-1429-341e-b397-ca2bb8f9cdfb", "obj_123456", "dolor sit amet", "St. Emm 57"));
    }

    @Test
    @DisplayName("idno-search field")
    void whenDescriptionIsMapped_thenIdnoSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getIdnoSearch(), is("Cod. ms. Bord. 1"));
    }

    @Test
    @DisplayName("idno-sort field")
    void whenDescriptionIsMapped_thenIdnoSortKeyIsMappedCorrectly() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getIdnoSortKey(), is("02f-0001-0005-0001"));
    }

    @Test
    @DisplayName("illuminated-search field")
    void whenDescriptionIsMapped_thenIlluminateSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getIlluminatedSearch(), is("yes"));
    }

    @Test
    @DisplayName("illuminated-facet field")
    void whenDescriptionIsMapped_thenIlluminatedFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getIlluminatedFacet(), arrayContainingInAnyOrder("yes"));
    }

    @Test
    @DisplayName("incipit-search field")
    void whenDescriptionIsMapped_thenIncipitSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getIncipitSearch(), arrayContainingInAnyOrder("Lorem ipsum dolor sit amet", "Abbatis Siculi repertorium n vii", "et dolore magna"));
    }

    @Test
    @DisplayName("item-iconography-search field")
    void whenDescriptionIsMapped_thenItemIconographySearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getItemIconographySearch(), is(Fixtures.LOREM_IPSUM_DESCRIPTION_ITEM_ICONOGRAPHY));
    }

    @Test
    @DisplayName("item-music-search field")
    void whenDescriptionIsMapped_thenItemMusicSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getItemMusicSearch(), is(Fixtures.LOREM_IPSUM_DESCRIPTION_ITEM_MUSIC));
    }

    @Test
    @DisplayName("item-text-search field")
    void whenDescriptionIsMapped_thenItemTextSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
      assertThat(loremIpsumDescription.getItemTextSearch(), is(Fixtures.LOREM_IPSUM_DESCRIPTION_ITEM_TEXT));
    }

    @Test
    @DisplayName("language-display field")
    void whenDescriptionIsMapped_thenLanguageDisplayIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getLanguageDisplay(), arrayContainingInAnyOrder("de", "la"));
    }

    @Test
    @DisplayName("language-facet field")
    void whenDescriptionIsMapped_thenLanguageFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getLanguageFacet(), arrayContainingInAnyOrder("de", "la"));
    }

    @Test
    @DisplayName("language-search field")
    void whenDescriptionIsMapped_thenLanguageSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getLanguageSearch(), arrayContainingInAnyOrder("de", "la", "la bg", "Texte in Deutsch, Latein und Bulgarisch", "lateinisch, deutsch"));
    }

    @Test
    @DisplayName("last-modified-display field")
    void whenDescriptionIsMapped_thenLastModifiedIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getLastModifiedDisplay(), DateMatchers.isDay(2020, Month.JANUARY, 2));
    }

    @Test
    @DisplayName("leaves-count-display field")
    void whenDescriptionIsMapped_thenLeavesCountDisplayIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getLeavesCountDisplay(), is("103 Bl., aus zwei Teilen zusammengesetzt"));
    }

    @Test
    @DisplayName("leaves-count-facet field")
    void whenDescriptionIsMapped_thenLeavesCountFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getLeavesCountFacet(), arrayContaining(103));
    }

    @Test
    @DisplayName("material-display field")
    void whenDescriptionIsMapped_thenMaterialDisplayIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getMaterialDisplay(), is("Mischung aus Papier und Leinen"));
    }

    @Test
    @DisplayName("material-facet field")
    void whenDescriptionIsMapped_thenMaterialFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getMaterialFacet(), arrayContainingInAnyOrder("linen", "paper"));
    }

    @Test
    @DisplayName("material-search field")
    void whenDescriptionIsMapped_thenMaterialSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getMaterialSearch(), arrayContaining("Mischung aus Papier und Leinen", "linen", "paper"));
    }

    @Test
    @DisplayName("object-type-facet field")
    void whenDescriptionIsMapped_thenObjectTypeFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getObjectTypeFacet(), arrayContaining("codex"));
    }

    @Test
    @DisplayName("object-type-search field")
    void whenDescriptionIsMapped_thenObjectTypeSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getObjectTypeSearch(), is("codex"));
    }

    @Test
    @DisplayName("orig-date-from-facet field")
    void whenDescriptionIsMapped_thenOrigDateFromFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getOrigDateFromFacet(), arrayContainingInAnyOrder(1651, -14));
    }

    @Test
    @DisplayName("orig-date-from-search field")
    void whenDescriptionIsMapped_thenOrigDateFromSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getOrigDateFromSearch(), arrayContainingInAnyOrder(1488, 1651, -14));
    }

    @Test
    @DisplayName("orig-date-to-facet field")
    void whenDescriptionIsMapped_thenOrigDateToFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getOrigDateToFacet(), arrayContainingInAnyOrder(15, 1680));
    }

    @Test
    @DisplayName("orig-date-to-search field")
    void whenDescriptionIsMapped_thenOrigDateToSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getOrigDateToSearch(), arrayContainingInAnyOrder(15, 1488, 1680));
    }

    @Test
    @DisplayName("orig-date-type-search field")
    void whenDescriptionIsMapped_thenOrigDateTypeSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getOrigDateTypeSearch(), arrayContainingInAnyOrder("datable", "dated"));
    }

    @Test
    @DisplayName("orig-date-lang-display field")
    void whenHspDescriptionIsMapped_thenOrigDateLangDisplayIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getOrigDateLangDisplay(), arrayContainingInAnyOrder("1488", "um 1665 (Teil II)", "um 0 (Teil III)"));
    }

    @Test
    @DisplayName("orig-date-when-facet field")
    void whenDescriptionIsMapped_thenOrigDateWhenFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getOrigDateWhenFacet(), arrayContainingInAnyOrder(1488));
    }

    @Test
    @DisplayName("orig-date-when-search field")
    void whenDescriptionIsMapped_thenOrigDateWhenSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getOrigDateWhenSearch(), arrayContainingInAnyOrder(1488));
    }

    @Test
    @DisplayName("orig-place-display field")
    void whenDescriptionIsMapped_thenOrigPlaceDisplayIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getOrigPlaceDisplay(), is("Staatsbibliothek zu Berlin, Berlin"));
    }

    @Test
    @DisplayName("orig-place-facet field")
    void whenDescriptionIsMapped_thenOrigPlaceFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getOrigPlaceFacet(), arrayContainingInAnyOrder("Staatsbibliothek zu Berlin", "Berlin"));
    }

    @Test
    @DisplayName("orig-place-search field")
    void whenDescriptionIsMapped_thenOrigPlaceSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getOrigPlaceSearch(), arrayContainingInAnyOrder("Staatsbibliothek zu Berlin, Berlin", "Staatsbibliothek zu Berlin", "Berlin"));
    }

    @Test
    @DisplayName("persistent-url-display field")
    void whenDescriptionIsMapped_thenPersistentURLDisplayIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersistentURLDisplay(), is("https://resolver.staatsbibliothek-berlin.de/__UUID__"));
    }

    @Test
    @DisplayName("person-author-search field")
    void whenDescriptionIsMapped_thenPersonAuthorIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersonAuthorSearch(), arrayContainingInAnyOrder("Corvinus, Antonius", "Martin Luther", "Georg Laubmann", "NORM-87370ae5-cf7c-3f3e-8adc-8dad1f9d2455", "NORM-258c6031-1128-325c-82ee-3859e6930fe1", "NORM-1afa34a7-f984-3eab-9bb0-a7d494132ee5"));
    }

    @Test
    @DisplayName("person-bookbinder-search field")
    void whenDescriptionIsMapped_thenPersonBookbinderSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersonBookbinderSearch(), arrayContainingInAnyOrder("Christian Hannick", "NORM-82161242-827b-303e-aacf-9c726942a1e4"));
    }

    @Test
    @DisplayName("person-commissioned-by-search field")
    void whenDescriptionIsMapped_thenPersonCommissionedSearchByIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersonCommissionedBySearch(), arrayContainingInAnyOrder("Otto Kresten", "NORM-38af8613-4b65-30f1-8fe3-3d30dd76442e"));
    }

    @Test
    @DisplayName("person-illuminator-search field")
    void whenDescriptionIsMapped_thenPersonIlluminatorSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersonIlluminatorSearch(), arrayContainingInAnyOrder("Wilhelm Meyer", "NORM-65ded535-3c5e-348d-8b7d-48c591b8f430"));
    }

    @Test
    @DisplayName("person-mentioned-search field")
    void whenDescriptionIsMapped_thenPersonMentionedSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersonMentionedInSearch(), arrayContainingInAnyOrder("Georg Thomas", "NORM-02522a2b-2726-3b0a-83bb-19f2d8d9524d"));
    }

    @Test
    @DisplayName("person-other-search field")
    void whenDescriptionIsMapped_thenPersonOtherSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersonOtherSearch(), arrayContainingInAnyOrder("Friedrich Keinz", "NORM-9fc3d715-2ba9-336a-a70e-36d0ed79bc43"));
    }

    @Test
    @DisplayName("person-previous-owner-search field")
    void whenDescriptionIsMapped_thenPersonPreviousOwnerSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersonPreviousOwnerSearch(), arrayContainingInAnyOrder("Brigitte Gullath", "Wolfgang Lackner", "NORM-96da2f59-0cd7-346b-bde0-051047b0d6f7", "NORM-7f1de29e-6da1-3d22-b51c-68001e7e0e54"));
    }

    @Test
    @DisplayName("person-conservator-search field")
    void whenDescriptionIsMapped_thenPersonConservatorSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersonConservatorSearch(), arrayContainingInAnyOrder("Johann Conrad Irmischer", "NORM-8f53295a-7387-3494-a9bc-8dd6c3c7104f"));
    }

    @Test
    @DisplayName("person-scribe-search field")
    void whenDescriptionIsMapped_thenPersonScribeSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersonScribeSearch(), arrayContainingInAnyOrder("Eduard Ippel", "NORM-8f855179-6779-3eee-b66c-225f7883bdcb"));
    }

    @Test
    @DisplayName("person-translator-search field")
    void whenDescriptionIsMapped_thenPersonTranslatorSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPersonTranslatorSearch(), arrayContainingInAnyOrder("Ingeborg Krekler", "NORM-045117b0-e0a1-3a24-ab97-65e79cbf113f"));
    }

    @Test
    @DisplayName("physical-description-search field")
    void whenDescriptionIsMapped_thenPhysicalDescriptionSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPhysicalDescriptionSearch(), is(Fixtures.PHYSICAL_DESCRIPTION));
    }

    @Test
    @DisplayName("publish-date-search field")
    void whenDescriptionIsMapped_thenPublishDateSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getPublishYearSearch(), is(2020));
    }

    @Test
    @DisplayName("repository-display field")
    void whenDescriptionIsMapped_thenRepositoryDisplayIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getRepositoryDisplay(), is("Universitätsbibliothek"));
    }

    @Test
    @DisplayName("repository-facet field")
    void whenDescriptionIsMapped_thenRepositoryFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getRepositoryFacet(), is(nullValue()));
    }

    @Test
    @DisplayName("repository-search field")
    void whenDescriptionIsMapped_thenRepositorySearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getRepositorySearch(), arrayContainingInAnyOrder("Universitätsbibliothek"));
    }

    @Test
    @DisplayName("settlement-display field")
    void whenDescriptionIsMapped_thenSettlementDisplayIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getSettlementDisplay(), is("Kiel"));
    }

    @Test
    @DisplayName("settlement-facet field")
    void whenDescriptionIsMapped_thenSettlementFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getSettlementFacet(), is(nullValue()));
    }

    @Test
    @DisplayName("settlement-search field")
    void whenDescriptionIsMapped_thenSettlementSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getSettlementSearch(), arrayContainingInAnyOrder("Kiel"));
    }

    @Test
    @DisplayName("status-facet field")
    void whenDescriptionIsMapped_thenStatusFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getStatusFacet(), arrayContainingInAnyOrder("existent"));
    }

    @Test
    @DisplayName("status-search field")
    void whenDescriptionIsMapped_thenStatusSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getStatusSearch(), is("existent"));
    }

    @Test
    @DisplayName("tei-document-display field")
    void whenDescriptionIsMapped_thenTEIDocumentDisplayIsCorrect() throws Exception {
      final byte[] tei = dataFromResourceFilename("fixtures/loremIpsum_beschreibung.xml");
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getTeiDocumentDisplay(), is(new String(tei)));
    }

    @Test
    @DisplayName("title-search field")
    void whenDescriptionIsMapped_thenTitleSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getTitleSearch(), equalTo("Catalogi bibliothecae Bordesholmensis, Bordesholmer Handschriften"));
    }

    @Test
    @DisplayName("type-search field")
    void whenHspObjectIsMapped_thenTypeSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getTypeSearch(), equalTo("hsp:description"));
    }

    @Test
    @DisplayName("with-facet field")
    void whenDescriptionIsMapped_thenWidthFacetIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getWidthFacet(), arrayContainingInAnyOrder(12F, 15.5F));
    }

    @Test
    @DisplayName("work-title-search field")
    void whenDescriptionIsMapped_thenWorkTitleSearchIsCorrect() throws Exception {
      final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(loremIpsumDescription.getWorkTitleSearch(), arrayContainingInAnyOrder("Lorem ipsum"));
    }

    @Nested
    @DisplayName("Register data")
    class DescriptionRegisterData {
      @Test
      @DisplayName("initium-search field")
      void givenDescriptionWithRegisterData_whenMapping_thenInitiumSearchIsCorrect() throws Exception {
        final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung_registerdaten.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
        assertThat(loremIpsumDescription.getInitiumSearch(), arrayContainingInAnyOrder(
            "Noble duc de Cleyves yssu de royalle lignee | Cousin du trescrestien et trespuissant roy", "Stet clita kasd"));
      }

      @Test
      @DisplayName("incipit-search field")
      void givenDescriptionWithRegisterData_whenMapping_thenIncipitSearchIsCorrect() throws Exception {
        final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung_registerdaten.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
        assertThat(loremIpsumDescription.getIncipitSearch(), arrayContainingInAnyOrder("parturient montes,"));
      }

      @Test
      @DisplayName("explicit-search field")
      void givenDescriptionWithRegisterData_whenMapping_thenExplicitSearchIsCorrect() throws Exception {
        final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung_registerdaten.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
        assertThat(loremIpsumDescription.getExplicitSearch(), arrayContainingInAnyOrder("Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo"));
      }

      @Test
      @DisplayName("quotation-search field")
      void givenDescriptionWithRegisterData_whenMapping_thenQuotationSearchIsCorrect() throws Exception {
        final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung_registerdaten.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
        assertThat(loremIpsumDescription.getQuotationSearch(), arrayContainingInAnyOrder("ligula eget", "massa. Cum", "sociis natoque", "penatibus et", "magnis dis"));
      }

      @Test
      @DisplayName("colophon-search field")
      void givenDescriptionWithRegisterData_whenMapping_thenColophonSearchIsCorrect() throws Exception {
        final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung_registerdaten.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
        assertThat(loremIpsumDescription.getColophonSearch(), arrayContainingInAnyOrder("dolor. Aenean"));
      }

      @Test
      @DisplayName("person-author-search field")
      void givenDescriptionWithRegisterData_whenMapping_thenPersonAuthorSearchIsCorrect() throws Exception {
        final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung_registerdaten.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
        assertThat(loremIpsumDescription.getPersonAuthorSearch(), arrayContainingInAnyOrder("Nulla consequat massa", "quis enim. Donec"));
      }

      @Test
      @DisplayName("person-author-search field")
      void givenDescriptionWithRegisterData_whenMapping_thenDescriptionAuthorIsNotMapped() throws Exception {
        final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung_registerdaten.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
        assertThat(loremIpsumDescription.getPersonAuthorSearch(), not(hasItemInArray("Katrin Sturm")));
      }

      @Test
      @DisplayName("person-scribe-search field")
      void givenDescriptionWithRegisterData_whenMapping_thenPersonScribeSearchIsCorrect() throws Exception {
        final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung_registerdaten.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
        assertThat(loremIpsumDescription.getPersonScribeSearch(), arrayContainingInAnyOrder("pede justo, fringilla"));
      }

      @Test
      @DisplayName("person-mentioned-in-search field")
      void givenDescriptionWithRegisterData_whenMapping_thenPersonMentionedInSearchIsCorrect() throws Exception {
        final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung_registerdaten.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
        assertThat(loremIpsumDescription.getPersonMentionedInSearch(), arrayContainingInAnyOrder("vulputate eget, arcu."));
      }

      @Test
      @DisplayName("person-previous-owner-search field")
      void givenDescriptionWithRegisterData_whenMapping_thenPersonPreviousOwnerSearchIsCorrect() throws Exception {
        final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung_registerdaten.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
        assertThat(loremIpsumDescription.getPersonPreviousOwnerSearch(), arrayContainingInAnyOrder("In enim justo,", "rhoncus ut, imperdiet"));
      }

      @Test
      @DisplayName("person-translator-search field")
      void givenDescriptionWithRegisterData_whenMapping_thenPersonTranslatorSearchIsCorrect() throws Exception {
        final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung_registerdaten.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
        assertThat(loremIpsumDescription.getPersonTranslatorSearch(), arrayContainingInAnyOrder("a, venenatis vitae,"));
      }

      @Test
      @DisplayName("person-illuminator-search field")
      void givenDescriptionWithRegisterData_whenMapping_thenPersonIlluminatorSearchIsCorrect() throws Exception {
        final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung_registerdaten.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
        assertThat(loremIpsumDescription.getPersonIlluminatorSearch(), arrayContainingInAnyOrder("felis eu pede"));
      }

      @Test
      @DisplayName("person-bookbinder-search field")
      void givenDescriptionWithRegisterData_whenMapping_thenPersonBookbinderSearchIsCorrect() throws Exception {
        final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung_registerdaten.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
        assertThat(loremIpsumDescription.getPersonBookbinderSearch(), arrayContainingInAnyOrder("tincidunt. Cras dapibus."));
      }

      @Test
      @DisplayName("person-commissioned-by-search field")
      void givenDescriptionWithRegisterData_whenMapping_thenPersonCommissionedBySearchIsCorrect() throws Exception {
        final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung_registerdaten.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
        assertThat(loremIpsumDescription.getPersonCommissionedBySearch(), arrayContainingInAnyOrder("leo ligula, porttitor", "eu, consequat vitae,"));
      }

      @Test
      @DisplayName("person-conservator-search field")
      void givenDescriptionWithRegisterData_whenMapping_thenPersonConservatorSearchIsCorrect() throws Exception {
        final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung_registerdaten.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
        assertThat(loremIpsumDescription.getPersonConservatorSearch(), arrayContainingInAnyOrder("nisi. Aenean vulputate", "eleifend tellus. Aenean"));
      }

      @Test
      @DisplayName("institution-mentioned-search field")
      void givenDescriptionWithRegisterData_whenMapping_thenInstitutionMentionedSearchIsCorrect() throws Exception {
        final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung_registerdaten.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
        assertThat(loremIpsumDescription.getInstitutionMentionedSearch(), arrayContainingInAnyOrder("Melrose", "Kloster Melrose"));
      }

      @Test
      @DisplayName("institution-previously-owning-search field")
      void givenDescriptionWithRegisterData_whenMapping_thenInstitutionPreviouslyOwningSearchIsCorrect() throws Exception {
        final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung_registerdaten.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
        assertThat(loremIpsumDescription.getInstitutionPreviouslyOwningSearch(), arrayContainingInAnyOrder("Erfurt", "Salvatorberg"));
      }

      @Test
      @DisplayName("institution-producing-search field")
      void givenDescriptionWithRegisterData_whenMapping_thenInstitutionProducingSearchIsCorrect() throws Exception {
        final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung_registerdaten.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
        assertThat(loremIpsumDescription.getInstitutionProducingSearch(), arrayContainingInAnyOrder("Inzigkofen", "Augustinerchorfrauenstift Inzigkofen"));
      }

      @Test
      @DisplayName("idno-alternative-search field")
      void givenDescriptionWithRegisterData_whenMapping_thenIdnoAltSearchIsCorrect() throws Exception {
        final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung_registerdaten.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
        assertThat(loremIpsumDescription.getIdnoAltSearch(), arrayContainingInAnyOrder("alt. Signatur"));
      }

      @Test
      @DisplayName("work-title-search field")
      void givenDescriptionWithRegisterData_whenMapping_thenWorkTitleSearchIsCorrect() throws Exception {
        final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung_registerdaten.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
        assertThat(loremIpsumDescription.getWorkTitleSearch(), arrayContainingInAnyOrder("Etiam rhoncus. Maecenas", "Nam eget dui."));
      }

      @Test
      @DisplayName("title-in-ms-search field")
      void givenDescriptionWithRegisterData_whenMapping_thenTitleInMsSearchIsCorrect() throws Exception {
        final HspDescription loremIpsumDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung_registerdaten.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));
        assertThat(loremIpsumDescription.getTitleInMsSearch(), arrayContainingInAnyOrder("massa. Cum", "sociis natoque", "penatibus et", "magnis dis"));
      }
    }

    @Test
    void givenTEIWithAuthorityFileIds_whenHspObjectIsMapped_thenAuthorityFileFacetIsCorrect() throws Exception {
      final HspDescription mappedDescription = mapDescriptionAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml", mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml"));

      assertThat(mappedDescription.getAuthorityFileFacet(), arrayContainingInAnyOrder(
          "NORM-1f0e3dad-9990-3345-b743-9f8ffabdffc4",
          "NORM-invalide-id",
          "NORM-1a75b353-14be-3c19-b22c-5c5334d78c8f",
          "NORM-9caa05ee-be18-3003-bfa9-454aa6349a9f",
          "NORM-654a4abc-3191-3e68-995b-4fdbd157cf9d",
          "NORM-26cf9267-82fe-3bf1-a37a-c9960658499f",
          "NORM-ee1611b6-1f56-38e7-8c12-b40684dbb395",
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
          "NORM-045117b0-e0a1-3a24-ab97-65e79cbf113f"
      ));
    }
  }

  @Nested
  @DisplayName("Group Mapping")
  class ObjectGroup {

    @Test
    @DisplayName("Injecting facet values with empty int values")
    void testInjectFacetFields_IntArray_EmptyKodValue() {
      HspObjectGroup group = new HspObjectGroup();
      final HspObject obj = getHspObject();
      final HspDescription firstDesc = getHspDescription();
      final HspDescription secondDesc = getHspDescription();
      obj.setLeavesCountFacet(new Integer[]{1});
      firstDesc.setLeavesCountFacet(new Integer[]{2, 3});
      secondDesc.setLeavesCountFacet(new Integer[]{4});
      group.setHspObject(obj);
      group.setHspDescriptions(List.of(firstDesc, secondDesc));

      group = FacetHelper.enrichFacets(group);

      assertThat(group.getHspObject().getLeavesCountFacet(), arrayContainingInAnyOrder(1, 2, 3, 4));
      assertThat(group.getHspDescriptions().get(0).getLeavesCountFacet(), arrayContainingInAnyOrder(1, 2, 3, 4));
      assertThat(group.getHspDescriptions().get(1).getLeavesCountFacet(), arrayContainingInAnyOrder(1, 2, 3, 4));
    }

    @Test
    @DisplayName("Injecting facet values with string values")
    void testInjectFacetFields_String() {
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
      group = FacetHelper.enrichFacets(group);

      assertThat(group.getHspObject().getFormatFacet(), arrayContainingInAnyOrder("format kod", "format desc 1", "format desc 2", "format desc 3"));
      assertThat(group.getHspDescriptions().get(0).getFormatFacet(), arrayContainingInAnyOrder("format kod", "format desc 1", "format desc 2", "format desc 3"));
      assertThat(group.getHspDescriptions().get(1).getFormatFacet(), arrayContainingInAnyOrder("format kod", "format desc 1", "format desc 2", "format desc 3"));
    }

    @Test
    @DisplayName("Injecting facet values with empty string values")
    void testInjectFacetFields_StringArray_EmptyKodValue() {
      HspObjectGroup group = new HspObjectGroup();
      group.setHspDescriptions(new ArrayList<>());
      final HspObject obj = getHspObject();

      final HspDescription firstDesc = getHspDescription();
      final HspDescription secondDesc = getHspDescription();

      firstDesc.setFormatFacet(new String[]{"format desc 1", "format desc 2"});
      secondDesc.setFormatFacet(new String[]{"format desc 3"});
      group.setHspObject(obj);
      group.setHspDescriptions(List.of(firstDesc, secondDesc));
      group = FacetHelper.enrichFacets(group);

      assertThat(group.getHspObject().getFormatFacet(), arrayContainingInAnyOrder("format desc 1", "format desc 2", "format desc 3"));
      assertThat(group.getHspDescriptions().get(0).getFormatFacet(), arrayContainingInAnyOrder("format desc 1", "format desc 2", "format desc 3"));
      assertThat(group.getHspDescriptions().get(1).getFormatFacet(), arrayContainingInAnyOrder("format desc 1", "format desc 2", "format desc 3"));
    }

    @Test
    @DisplayName("Injecting facet values - depth")
    void whenActivityStreamMessageIsMapped_thenDepthFacetIsCorrect() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), getDescriptionContents()).get();

      assertThat(group.getHspObject().getDepthFacet(), arrayContainingInAnyOrder(2f, 3f));
      assertThat(group.getHspDescriptions().get(0).getDepthFacet(), arrayContainingInAnyOrder(2f, 3f));
      assertThat(group.getHspDescriptions().get(1).getDepthFacet(), arrayContainingInAnyOrder(2f, 3f));
    }

    @Test
    @DisplayName("Injecting facet values - format")
    void whenActivityStreamMessageIsMapped_thenFormatFacetIsCorrect() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), getDescriptionContents()).get();

      assertThat(group.getHspObject().getFormatFacet(), arrayContainingInAnyOrder("folio", "oblong", "octavo"));
      assertThat(group.getHspDescriptions().get(0).getFormatFacet(), arrayContainingInAnyOrder("folio", "oblong", "octavo"));
      assertThat(group.getHspDescriptions().get(1).getFormatFacet(), arrayContainingInAnyOrder("folio", "oblong", "octavo"));
    }

    @Test
    @DisplayName("Injecting facet values - object type")
    void whenActivityStreamMessageIsMapped_thenFormatTypeFacetIsCorrect() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), getDescriptionContents()).get();

      assertThat(group.getHspObject().getFormatTypeFacet(), arrayContainingInAnyOrder("factual", "deduced", "computed"));
      assertThat(group.getHspDescriptions().get(0).getFormatTypeFacet(), arrayContainingInAnyOrder("factual", "deduced", "computed"));
      assertThat(group.getHspDescriptions().get(1).getFormatTypeFacet(), arrayContainingInAnyOrder("factual", "deduced", "computed"));
    }

    @Test
    @DisplayName("Injecting facet values - height")
    void whenActivityStreamMessageIsMapped_thenHeightFacetIsCorrect() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), getDescriptionContents()).get();

      assertThat(group.getHspObject().getHeightFacet(), arrayContainingInAnyOrder(12.5f, 16f, 17f));
      assertThat(group.getHspDescriptions().get(0).getHeightFacet(), arrayContainingInAnyOrder(12.5f, 16f, 17f));
      assertThat(group.getHspDescriptions().get(1).getHeightFacet(), arrayContainingInAnyOrder(12.5f, 16f, 17f));
    }

    @Test
    @DisplayName("Injecting facet values - leaves count")
    void whenActivityStreamMessageIsMapped_thenLeavesCountFacetIsCorrect() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), getDescriptionContents()).get();

      assertThat(group.getHspObject().getLeavesCountFacet(), arrayContainingInAnyOrder(102, 103));
      assertThat(group.getHspDescriptions().get(0).getLeavesCountFacet(), arrayContainingInAnyOrder(102, 103));
      assertThat(group.getHspDescriptions().get(1).getLeavesCountFacet(), arrayContainingInAnyOrder(102, 103));
    }

    @Test
    @DisplayName("Injecting facet values - material")
    void whenActivityStreamMessageIsMapped_thenMaterialFacetIsCorrect() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), getDescriptionContents()).get();

      assertThat(group.getHspObject().getMaterialFacet(), arrayContainingInAnyOrder("linen", "paper", "palm", "papyrus"));
      assertThat(group.getHspDescriptions().get(0).getMaterialFacet(), arrayContainingInAnyOrder("linen", "paper", "palm", "papyrus"));
      assertThat(group.getHspDescriptions().get(1).getMaterialFacet(), arrayContainingInAnyOrder("linen", "paper", "palm", "papyrus"));
    }

    @Test
    @DisplayName("Injecting facet values - object type")
    void whenActivityStreamMessageIsMapped_thenObjectTypeFacetIsCorrect() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), getDescriptionContents()).get();

      assertThat(group.getHspObject().getObjectTypeFacet(), arrayContainingInAnyOrder("codex", "leporello"));
      assertThat(group.getHspDescriptions().get(0).getObjectTypeFacet(), arrayContainingInAnyOrder("codex", "leporello"));
      assertThat(group.getHspDescriptions().get(1).getObjectTypeFacet(), arrayContainingInAnyOrder("codex", "leporello"));
    }

    @Test
    @DisplayName("Injecting facet values - orig date when")
    void whenActivityStreamMessageIsMapped_thenOrigDateWhenFacetIsCorrect() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), getDescriptionContents()).get();

      assertThat(group.getHspObject().getOrigDateWhenFacet(), arrayContainingInAnyOrder(1488, 1487));
      assertThat(group.getHspDescriptions().get(0).getOrigDateWhenFacet(), arrayContainingInAnyOrder(1488, 1487));
      assertThat(group.getHspDescriptions().get(1).getOrigDateWhenFacet(), arrayContainingInAnyOrder(1488, 1487));
    }

    @Test
    @DisplayName("Injecting facet values - status")
    void whenActivityStreamMessageIsMapped_thenStatusFacetIsCorrect() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), getDescriptionContents()).get();

      assertThat(group.getHspObject().getStatusFacet(), arrayContainingInAnyOrder("existent"));
      assertThat(group.getHspDescriptions().get(0).getStatusFacet(), arrayContainingInAnyOrder("existent"));
      assertThat(group.getHspDescriptions().get(1).getStatusFacet(), arrayContainingInAnyOrder("existent"));
    }

    @Test
    @DisplayName("Injecting facet values - width")
    void whenActivityStreamMessageIsMapped_thenWidthFacetIsCorrect() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), getDescriptionContents()).get();

      assertThat(group.getHspObject().getWidthFacet(), arrayContainingInAnyOrder(12f, 15.5f, 13f));
      assertThat(group.getHspDescriptions().get(0).getWidthFacet(), arrayContainingInAnyOrder(12f, 15.5f, 13f));
      assertThat(group.getHspDescriptions().get(1).getWidthFacet(), arrayContainingInAnyOrder(12f, 15.5f, 13f));
    }

  @Test
  @DisplayName("Repository id facet digitized")
    void whenActivityStreamMessageIsMapped_thenRepositoryIdFascetOfDigitizedIsCorrect() throws Exception {
      AuthorityFileFixture.runWithAuthorityFileService(() -> {
        final HspObjectGroup group = solrMapper.mapHspObjectGroup(dataFromResourceFilename("fixtures/loremIpsum_kod_digitalisat_iiif.xml"), getDescriptionContents()).get();

      assertThat(group.getHspDigitized(), hasSize(1));
        assertThat(group.getHspDigitized().get(0).getRepositoryIdFacet(), is("NORM-9caa05ee-be18-3003-bfa9-454aa6349a9f"));
      }, solrMapper);
    }

    @Test
    @DisplayName("Repository id facet hsp description / object")
    void whenHspDescriptionStreamMessageIsMapped_thenRepositoryIdFacetIsCorrect() throws Exception {
        final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), getDescriptionContents()).get();
      assertThat(group.getHspDescriptions().get(0).getRepositoryIdFacet(), is("NORM-9caa05ee-be18-3003-bfa9-454aa6349a9f"));
      assertThat(group.getHspObject().getRepositoryIdFacet(), is("NORM-9caa05ee-be18-3003-bfa9-454aa6349a9f"));

    }

    @Test
    void whenHspObjectWithoutHspDescriptionIsMapped_DescribedObjectSearchIsFalse() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(getKODContent(), Collections.emptyList()).get();

      assertThat(group.getHspObject()
          .getDescribedObjectSearch(), equalTo(Boolean.FALSE));
    }

    @Test
    void givenHspObjectGroup_whenMapping_thenDigitizedContainsCorrectKodId() throws Exception {
      final HspObjectGroup group = solrMapper.mapHspObjectGroup(dataFromResourceFilename("fixtures/loremIpsum_kod_digitalisat_iiif.xml"), Collections.emptyList()).get();

      assertThat(group.getHspDigitized().get(0).getKodIdDisplay(), equalTo(group.getHspObject().getId()));
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
      assertThat(MappingHelper.getMsIdentifierFromSettlementRepositoryIdno(hspObject), is("Leipzig, Universitätsbibliothek Leipzig, MS 1234"));
      assertThat(MappingHelper.getMsIdentifierFromSettlementRepositorySortKeyOrIdno(hspObject), equalTo("Leipzig, Universitätsbibliothek Leipzig, 12345"));
    }

    @Test
    void whenGetMsIdentifierFromSettlementRepositorySortKeyOrIdnoIsCalledAndSortKeyNotExists_thenIdnoIsUsed() {
      HspObject hspObject = new HspObject();
      hspObject.setIdnoSearch("MS 1234");
      hspObject.setSettlementDisplay("Leipzig");
      hspObject.setRepositoryDisplay("Universitätsbibliothek Leipzig");
      assertThat(MappingHelper.getMsIdentifierFromSettlementRepositoryIdno(hspObject), is("Leipzig, Universitätsbibliothek Leipzig, MS 1234"));
      assertThat(MappingHelper.getMsIdentifierFromSettlementRepositorySortKeyOrIdno(hspObject), is("Leipzig, Universitätsbibliothek Leipzig, MS 1234"));
    }

    @Test
    void whenHspObjectContainsOrigDateSortingInformation_thenAssociatedDescriptionWillAlsoDo() throws Exception {
      final HspObject loremIpsumDescription = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml");

      assertThat(loremIpsumDescription.getOrigDateFromSort(), is(loremIpsumDescription.getOrigDateFromSort()));
      assertThat(loremIpsumDescription.getOrigDateToSort(), is(loremIpsumDescription.getOrigDateToSort()));
    }

    @Test
    void whenHspObjectIsMappedWithAuthorityFileService_origPlaceContainsAllAuthorityFileInformation() throws Exception {
      AuthorityFileFixture.runWithAuthorityFileService(() -> {
        final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");
        assertThat(loremIpsumKOD.getOrigPlaceSearch(), arrayContainingInAnyOrder("5036103-X", "NORM-774909e2-f687-30cb-a5c4-ddc95806d6be", "DE-1", "soz_30002258", "Dt. SB", "Staatsbibliothek zu Berlin - Preußischer Kulturbesitz", "Gosudarstvennaja Biblioteka v Berline - Prusskoe Kulʹturnoe Nasledie", "SBPK", "Bibliotheca Regia Berolinensis", "Staatsbibliothek zu Berlin, Berlin", "Staatsbibliothek zu Berlin", "Berlin"));
        assertThat(loremIpsumKOD.getOrigPlaceDisplay(), is("Staatsbibliothek zu Berlin, Berlin"));
        assertThat(loremIpsumKOD.getOrigPlaceFacet(), arrayContainingInAnyOrder("Staatsbibliothek zu Berlin"));
      }, solrMapper);
    }

    @Test
    void whenHspObjectIsMappedWithoutAuthorityFileService_origPlaceContainsOnlyOrigPlaceFromIndexFieldAndDefaultValues() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigPlaceSearch(), arrayContainingInAnyOrder("Staatsbibliothek zu Berlin, Berlin", "Staatsbibliothek zu Berlin", "Berlin"));
      assertThat(loremIpsumKOD.getOrigPlaceDisplay(), is("Staatsbibliothek zu Berlin, Berlin"));
    }

    @Test
    void whenHspObjectIsMapped_thenDepthFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getDepthFacet(), is(new float[]{2}));
    }

    @Test
    void whenHspObjectIsMapped_thenDimensionsDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getDimensionsDisplay(), arrayContainingInAnyOrder("16 × 12 (Teil I)", "12,5 × 15,5 (Teil II)"));
    }

    @Test
    void whenHspObjectIsMapped_thenSettlementAuthorityFileDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getSettlementAuthorityFileDisplay(), arrayContainingInAnyOrder("NORM-1a75b353-14be-3c19-b22c-5c5334d78c8f"));
    }

    @Test
    void whenHspObjectIsMapped_thenOrigPlaceAuthorityFileDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigPlaceAuthorityFileDisplay(), arrayContainingInAnyOrder("NORM-ee1611b6-1f56-38e7-8c12-b40684dbb395"));
    }

    @Test
    void whenHspObjectIsMapped_thenRepositoryAuthorityFileIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getRepositoryAuthorityFileDisplay(), arrayContainingInAnyOrder("NORM-9caa05ee-be18-3003-bfa9-454aa6349a9f"));
    }

    @Test
    @DisplayName("format-facet field")
    void whenHspObjectIsMapped_thenFormatFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getFormatFacet(), arrayContaining("folio", "oblong"));
    }

    @Test
    void whenHspObjectIsMapped_thenFormatIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getFormatSearch(), arrayContainingInAnyOrder("folio", "oblong"));
      assertThat(loremIpsumKOD.getFormatFacet(), is(loremIpsumKOD.getFormatSearch()));
    }

    @Test
    @DisplayName("format-search field")
    void whenHspObjectIsMapped_thenFormatSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getFormatSearch(), arrayContainingInAnyOrder("folio", "oblong"));
    }

    @Test
    void whenHspObjectIsMapped_thenFormatTypeDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getFormatTypeDisplay(), arrayContainingInAnyOrder("deduced", "factual"));
    }

    @Test
    void whenHspObjectIsMapped_thenFormatTypeFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getFormatTypeFacet(), arrayContainingInAnyOrder("deduced", "factual"));
    }

    @Test
    void whenHspObjectIsMapped_thenHasNotationDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getHasNotationDisplay(), is("no"));
    }

    @Test
    void whenHspObjectIsMapped_thenHasNotationFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getHasNotationFacet(), arrayContainingInAnyOrder("no"));
    }

    @Test
    @DisplayName("height-facet field")
    void whenHspObjectIsMapped_thenHeightFacetIsCorrect() throws Exception {
      final HspObject loremIpsumDescription = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumDescription.getHeightFacet(), arrayContainingInAnyOrder(16F, 12.5F));
    }

    @Test
    void whenHspObjectIsMapped_thenIdIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getId(), is("4de2ec4a-09e0-11ee-be56-0242ac120002"));
    }

    @Test
    void whenHspObjectIsMapped_thenIdnoAltSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getIdnoAltSearch(), arrayContainingInAnyOrder("St. Emm 57", "Cmb 1", "C.m.b. 1"));
    }

    @Test
    void whenHspObjectIsMapped_thenIdnoSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");
      assertThat(loremIpsumKOD.getIdnoSearch(), is("Cod. ms. Bord. 1"));
    }

    @Test
    void whenHspObjectIsMapped_thenIlluminateDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getIlluminatedDisplay(), is("yes"));
    }

    @Test
    void whenHspObjectIsMapped_thenIlluminatedFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getIlluminatedFacet(), arrayContainingInAnyOrder("yes"));
    }

    @Test
    void whenHspObjectIsMapped_thenLanguageDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getLanguageDisplay(), arrayContainingInAnyOrder("de", "la"));
    }

    @Test
    void whenHspObjectIsMapped_thenLanguageFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getLanguageFacet(), arrayContainingInAnyOrder("de", "la"));
    }

    @Test
    void whenHspObjectIsMapped_thenLanguageSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getLanguageSearch(), arrayContainingInAnyOrder("latein und deutsch", "de", "la"));
    }

    @Test
    void whenHspObjectIsMapped_thenLastModifiedIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getLastModifiedDisplay(), DateMatchers.isDay(2022, Month.NOVEMBER, 24));
    }

    @Test
    @DisplayName("leaves-count-display field")
    void whenHspObjectIsMapped_thenLeavesCountDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getLeavesCountDisplay(), is("103 Bl., aus zwei Teilen zusammengesetzt"));
    }

    @Test
    @DisplayName("leaves-count-facet field")
    void whenHspObjectIsMapped_thenLeavesCountFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getLeavesCountFacet(), arrayContaining(103));
    }

    @Test
    @DisplayName("material-display field")
    void whenHspObjectIsMapped_thenMaterialDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getMaterialDisplay(), is("Mischung aus Papier und Leinen"));
    }

    @Test
    @DisplayName("material-facet field")
    void whenHspObjectIsMapped_thenMaterialFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getMaterialFacet(), arrayContainingInAnyOrder("linen", "paper"));
    }

    @Test
    @DisplayName("material-search field")
    void whenHspObjectIsMapped_thenMaterialSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getMaterialSearch(), arrayContaining("Mischung aus Papier und Leinen", "linen", "paper"));
    }

    @Test
    @DisplayName("object-type-facet field")
    void whenHspObjectIsMapped_thenObjectTypeFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getObjectTypeFacet(), arrayContainingInAnyOrder("codex"));
    }

    @Test
    @DisplayName("object-type-search field")
    void whenHspObjectIsMapped_thenObjectTypeSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getObjectTypeSearch(), is("codex"));
    }

    @Test
    @DisplayName("orig-date-from-facet field")
    void whenHspObjectIsMapped_thenOrigDateFromFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigDateFromFacet(), arrayContainingInAnyOrder(1651, -14));
    }

    @Test
    @DisplayName("orig-date-from-search field")
    void whenHspObjectIsMapped_thenOrigDateFromSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigDateFromSearch(), arrayContainingInAnyOrder(1651, -14, 1488));
    }

    @Test
    @DisplayName("orig-date-type-facet field")
    void whenHspObjectIsMapped_thenOrigDateTypeFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigDateTypeFacet(), arrayContainingInAnyOrder("datable", "dated"));
    }

    @Test
    @DisplayName("orig-date-to-facet field")
    void whenHspObjectIsMapped_thenOrigDateToFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigDateToFacet(), arrayContainingInAnyOrder(15, 1680));
    }

    @Test
    @DisplayName("orig-date-to-search field")
    void whenHspObjectIsMapped_thenOrigDateToSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigDateToSearch(), arrayContainingInAnyOrder(15, 1680, 1488));
    }

    @Test
    @DisplayName("orig-date-lang-display field")
    void whenHspObjectIsMapped_thenOrigDateLangDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigDateLangDisplay(), arrayContainingInAnyOrder("1488", "um 1665 (Teil II)", "um 0 (Teil III)"));
    }

    @Test
    @DisplayName("orig-date-when-facet field")
    void whenHspObjectIsMapped_thenOrigDateWhenFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigDateWhenFacet(), arrayContainingInAnyOrder(1488));
    }

    @Test
    @DisplayName("orig-date-when-search field")
    void whenHspObjectIsMapped_thenOrigDateWhenSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigDateWhenSearch(), arrayContainingInAnyOrder(1488));
    }

    @Test
    @DisplayName("orig-place-display field")
    void whenHspObjectIsMapped_thenOrigPlaceDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigPlaceDisplay(), is("Staatsbibliothek zu Berlin, Berlin"));
    }

    @Test
    @DisplayName("orig-place-facet field")
    void whenHspObjectIsMapped_thenOrigPlaceFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigPlaceFacet(), arrayContainingInAnyOrder("Staatsbibliothek zu Berlin", "Berlin"));
    }

    @Test
    @DisplayName("orig-place-search field")
    void whenHspObjectIsMapped_thenOrigPlaceSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getOrigPlaceSearch(), arrayContainingInAnyOrder("Staatsbibliothek zu Berlin, Berlin", "Staatsbibliothek zu Berlin", "Berlin"));
    }

    @Test
    @DisplayName("persistent-url-display field")
    void whenHspObjectIsMapped_thenPersistentURLDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getPersistentURLDisplay(), is("https://resolver.staatsbibliothek-berlin.de/__UUID__"));
    }

    @Test
    @DisplayName("repository-display field")
    void whenHspObjectIsMapped_thenRepositoryDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getRepositoryDisplay(), is("Universitätsbibliothek"));
    }

    @Test
    @DisplayName("repository-facet field")
    void whenHspObjectIsMapped_thenRepositoryFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getRepositoryFacet(), is(nullValue()));
    }

    @Test
    @DisplayName("repository-search field")
    void whenHspObjectIsMapped_thenRepositorySearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getRepositorySearch(), arrayContainingInAnyOrder("Universitätsbibliothek"));
    }

    @Test
    @DisplayName("settlement-display field")
    void whenHspObjectIsMapped_thenSettlementDisplayIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getSettlementDisplay(), is("Kiel"));
    }

    @Test
    @DisplayName("settlement-facet field")
    void whenHspObjectIsMapped_thenSettlementFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getSettlementFacet(), is(nullValue()));
    }

    @Test
    @DisplayName("settlement-search field")
    void whenHspObjectIsMapped_thenSettlementSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getSettlementSearch(), arrayContainingInAnyOrder("Kiel"));
    }

    @Test
    @DisplayName("status-facet field")
    void whenHspObjectIsMapped_thenStatusFacetIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getStatusFacet(), arrayContainingInAnyOrder("existent"));
    }

    @Test
    @DisplayName("status-search field")
    void whenHspObjectIsMapped_thenStatusSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getStatusSearch(), is("existent"));
    }

    @Test
    @DisplayName("tei-document-display field")
    void whenHspObjectIsMapped_thenTEIDocumentDisplayIsCorrect() throws Exception {
      final byte[] tei = dataFromResourceFilename("fixtures/loremIpsum_beschreibung.xml");
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_beschreibung.xml");

      assertThat(loremIpsumKOD.getTeiDocumentDisplay(), is(new String(tei)));
    }

    @Test
    @DisplayName("title-search field")
    void whenHspObjectIsMapped_thenTitleSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getTitleSearch(), equalTo("Catalogi bibliothecae Bordesholmensis, Bordesholmer Handschriften"));
    }

    @Test
    @DisplayName("type-search field")
    void whenHspObjectIsMapped_thenTypeSearchIsCorrect() throws Exception {
      final HspObject loremIpsumKOD = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumKOD.getTypeSearch(), equalTo("hsp:object"));
    }

    @Test
    @DisplayName("width-facet field")
    void whenHspObjectIsMapped_thenWidthFacetIsCorrect() throws Exception {
      final HspObject loremIpsumDescription = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(loremIpsumDescription.getWidthFacet(), arrayContainingInAnyOrder(12F, 15.5F));
    }

    @Test
    void givenTEIWithoutOrigDateDated_whenHspObjectIsMapped_thenOrigDateFacetIsEmpty() throws Exception {
      final HspObject loremIpsumKODModified = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod_modifiziert.xml");

      assertThat(loremIpsumKODModified.getOrigDateWhenFacet(), nullValue());
    }

    @Test
    void whenHspObjectIsMapped_thenFormerRepositoryIsCorrect() throws Exception {
      AuthorityFileFixture.runWithAuthorityFileService(() -> {
        final HspObject result = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");
        assertThat(result.getInstitutionPreviouslyOwningSearch(), arrayContainingInAnyOrder("Kloster Sankt Emmeram Regensburg"));
        assertThat(result.getFormerIdnoSearch(), arrayContainingInAnyOrder("St. Emm 57"));
      }, solrMapper);
    }

    @Test
    void givenTEIWithAuthorityFileIds_whenHspObjectIsMapped_thenAuthorityFileFacetIsCorrect() throws Exception {
      final HspObject mappedHspObject = mapHspObjectAndAssertNotEmpty("fixtures/loremIpsum_kod.xml");

      assertThat(mappedHspObject.getAuthorityFileFacet(), arrayContainingInAnyOrder(
          "NORM-1a75b353-14be-3c19-b22c-5c5334d78c8f",
          "NORM-9caa05ee-be18-3003-bfa9-454aa6349a9f",
          "NORM-26cf9267-82fe-3bf1-a37a-c9960658499f",
          "NORM-654a4abc-3191-3e68-995b-4fdbd157cf9d",
          "NORM-ee1611b6-1f56-38e7-8c12-b40684dbb395"
      ));
    }
  }

  @Nested
  class Catalog {
    @Test
    void givenCatalogTEI_whenMapping_thenAuthorDisplayIsCorrect() throws Exception {
      final HspCatalog catalogue = mapHspCatalog("fixtures/loremIpsum_catalog.xml");
      assertThat(catalogue.getAuthorDisplay(), arrayContainingInAnyOrder("Thomas Falmagne"));
    }

    @Test
    void givenCatalogTEI_whenMapping_thenAuthorFacetIsCorrect() throws Exception {
      final HspCatalog catalogue = mapHspCatalog("fixtures/loremIpsum_catalog.xml");
      assertThat(catalogue.getAuthorFacet(), arrayContainingInAnyOrder("Thomas Falmagne"));
    }

    @Test
    void givenCatalogTEI_whenMapping_thenAuthorSearchIsCorrect() throws Exception {
      final HspCatalog catalogue = mapHspCatalog("fixtures/loremIpsum_catalog.xml");
      assertThat(catalogue.getAuthorSearch(), arrayContainingInAnyOrder("Thomas Falmagne"));
    }

    @Test
    void givenCatalogTEI_whenMapping_thenEditorDisplayIsCorrect() throws Exception {
      final HspCatalog catalogue = mapHspCatalog("fixtures/loremIpsum_catalog.xml");
      assertThat(catalogue.getEditorDisplay(), arrayContainingInAnyOrder("Armin Dietzel", "Günther Bauer"));
    }

    @Test
    void givenCatalogTEI_whenMapping_thenEditorSearchIsCorrect() throws Exception {
      final HspCatalog catalogue = mapHspCatalog("fixtures/loremIpsum_catalog.xml");
      assertThat(catalogue.getEditorSearch(), arrayContainingInAnyOrder("Armin Dietzel", "Günther Bauer"));
    }

    @Test
    void givenCatalogTEI_whenMapping_thenFulltextSearchIsCorrect() throws Exception {
      final HspCatalog catalogue = mapHspCatalog("fixtures/loremIpsum_catalog.xml");
      assertThat(catalogue.getFullTextSearch(), is("DIE HANDSCHRIFTEN DES GROSSHERZOGTUMS LUXEMBURG herausgegeben von der Bibliotheque nationale de Luxembourg Band 2"));
    }

    @Test
    void givenCatalogTEI_whenMapping_thenIdSearchIsCorrect() throws Exception {
      final HspCatalog catalogue = mapHspCatalog("fixtures/loremIpsum_catalog.xml");
      assertThat(catalogue.getId(), is("HSP-013a006f-03db-3539-aeff-eb8f18fda755"));
    }

    @Test
    void givenCatalogTEI_whenMapping_thenPublisherSearchIsCorrect() throws Exception {
      final HspCatalog catalogue = mapHspCatalog("fixtures/loremIpsum_catalog.xml");

      assertThat(catalogue.getPublisherSearch(), arrayContainingInAnyOrder("Harrassowitz"));
    }

    @Test
    void givenCatalogTEI_whenMapping_thenPublisherFacetIsCorrect() throws Exception {
      final HspCatalog catalogue = mapHspCatalog("fixtures/loremIpsum_catalog.xml");

      assertThat(catalogue.getPublisherFacet(), arrayContainingInAnyOrder("Harrassowitz"));
    }

    @Test
    void givenCatalogTEI_whenMapping_thenPublishYearFacetIsCorrect() throws Exception {
      final HspCatalog catalogue = mapHspCatalog("fixtures/loremIpsum_catalog.xml");

      assertThat(catalogue.getPublishYearFacet(), arrayContainingInAnyOrder(2017));
    }

    @Test
    void givenCatalogTEI_whenMapping_thenPublishYearSearchIsCorrect() throws Exception {
      final HspCatalog catalogue = mapHspCatalog("fixtures/loremIpsum_catalog.xml");

      assertThat(catalogue.getPublishYearSearch(), is(2017));
    }


    @Test
    void givenCatalogTEI_whenMapping_thenTitleSearchIsCorrect() throws Exception {
      final HspCatalog catalogue = mapHspCatalog("fixtures/loremIpsum_catalog.xml");
      assertThat(catalogue.getTitleSearch(), is("Titel des Katalogs"));
    }

    @Test
    void givenCatalogTEI_whenMapping_thenTypeSearchIsCorrect() throws Exception {
      final HspCatalog catalogue = mapHspCatalog("fixtures/loremIpsum_catalog.xml");

      assertThat(catalogue.getTypeSearch(), is("hsp:catalog"));
    }

    @Test
    void givenCatalogTEI_whenMapping_thenTEIDocumentDisplayIsCorrect() throws Exception {
      final HspCatalog catalogue = mapHspCatalog("fixtures/loremIpsum_catalog.xml");
      final String tei = new String(dataFromResourceFilename("fixtures/loremIpsum_catalog.xml"));
      assertThat(catalogue.getTeiDocumentDisplay(), is(tei));
    }

    @Test
    void givenCatalogTEI_whenMapping_thenThumbnailDisplayIsCorrect() throws Exception {
      final HspCatalog catalogue = mapHspCatalog("fixtures/loremIpsum_catalog.xml");

      assertThat(catalogue.getThumbnailUrlDisplay(), is("https://iiif.ub.uni-leipzig.de/iiif/j2k/0000/0354/0000035441/00000003.jpx/full/200,/0/default.jpg"));
    }

    @Test
    void givenCatalogTEI_whenMapping_thenManifestURIDisplayIsCorrect() throws Exception {
      final HspCatalog catalogue = mapHspCatalog("fixtures/loremIpsum_catalog.xml");

      assertThat(catalogue.getManifestURIDisplay(), is("https://iiif.ub.uni-leipzig.de/0000035441/manifest.json"));
    }

    @Test
    void givenCatalogTEI_whenMapping_thenRepositoryFacetIsCorrect() throws Exception {
      final HspCatalog catalogue = mapHspCatalog("fixtures/loremIpsum_catalog.xml");

      assertThat(catalogue.getRepositoryFacet(), is("Aachen, StB"));
    }
  }
}