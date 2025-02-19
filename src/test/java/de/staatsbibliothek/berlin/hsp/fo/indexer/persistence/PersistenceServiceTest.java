package de.staatsbibliothek.berlin.hsp.fo.indexer.persistence;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.Constants;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.PersistenceServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.type.HspObjectType;
import org.apache.solr.common.SolrException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersistenceServiceTest extends BaseSolrTest {

  private static final HspBaseTestClass HSP_BASE_OBJECT = new HspBaseTestClass("testid", HspObjectType.OBJECT.getValue()[0]);
  private static final HspBaseTestClass HSP_BASE_OBJECT_INVALID = new HspBaseTestClass("", HspObjectType.OBJECT.getValue()[0]);
  private final PersistenceServiceImpl<HspBaseTestClass> persistenceService;

  public PersistenceServiceTest() {
    super();
    persistenceService = new PersistenceServiceImpl<HspBaseTestClass>(HspBaseTestClass.class);
  }

  @BeforeEach
  void setup() throws Exception {
    persistenceService.setSolrClient(embeddedSolr.getSolrServer()
        .getSolrClient("test"));
    persistenceService.setCollectionName("test");
  }

  @Test
  void whenAddIsCalled_ThenObjectShouldBeAdded() throws Exception {
    persistenceService.add(HSP_BASE_OBJECT);

    final Collection<HspBaseTestClass> loaded = persistenceService.find("id-search:testid");
    assertThat(loaded, hasSize(1));
    assertThat(loaded, hasItem(HSP_BASE_OBJECT));
  }

  @Test
  void whenAddWithInvalidObjectIsCalled_ThenObjectShouldNotBeAdded() throws Exception {
    final Exception solrException = assertThrows(SolrException.class, () -> persistenceService.add(HSP_BASE_OBJECT_INVALID));
    assertThat(solrException.getMessage(), is("Document is missing mandatory uniqueKey field: id-search"));

    final Collection<HspBaseTestClass> loaded = persistenceService.find("id-search:testid");
    assertThat(loaded, hasSize(0));
  }

  @Test
  void whenAddAllIsCalled_ThenAllObjectsShouldBeSaved() throws Exception {
    final HspBaseTestClass testObj = new HspBaseTestClass("testid", HspObjectType.OBJECT.getValue()[0]);
    final HspBaseTestClass testObj2 = new HspBaseTestClass("testid2", HspObjectType.OBJECT.getValue()[0]);
    persistenceService.addAll(List.of(testObj, testObj2));
    final Collection<HspBaseTestClass> loaded = persistenceService.find("*:*");

    assertThat(loaded, hasSize(2));
    assertThat(loaded, hasItems(testObj, testObj2));
  }

  @Test
  void whenAddAllIsCalledWithInvalidObject_ThenNoneOfTheCollectionsObjectsShouldBeAdded() throws Exception {
    final Collection<HspBaseTestClass> objectsToAdd = List.of(HSP_BASE_OBJECT_INVALID, HSP_BASE_OBJECT);
    Collection<HspBaseTestClass> loaded = persistenceService.find("*:*");
    final int sizeBefore = loaded.size();

    final Exception solrException = assertThrows(PersistenceServiceException.class, () -> persistenceService.addAll(objectsToAdd));
    assertThat(solrException.getMessage(), is("Document is missing mandatory uniqueKey field: id-search"));
    loaded = persistenceService.find("*:*");

    assertThat(loaded, hasSize(sizeBefore));
  }

  @Test
  void whenRemoveByQueryIsCalledWithMatchingQuery_ThenAllMatchingObjectsShouldBeRemoved() throws Exception {
    final HspBaseTestClass testObj = new HspBaseTestClass("testid", HspObjectType.OBJECT.getValue()[0]);
    final HspBaseTestClass testObj2 = new HspBaseTestClass("testid2", HspObjectType.OBJECT.getValue()[0]);
    final HspBaseTestClass testDesc = new HspBaseTestClass("testid3", HspObjectType.DESCRIPTION.getValue()[0]);

    persistenceService.addAll(List.of(testObj, testObj2, testDesc));

    Collection<HspBaseTestClass> loaded = persistenceService.find("*:*");
    assertThat(loaded, hasSize(3));

    persistenceService.remove(String.format("type-search:\"%s\"", HspObjectType.OBJECT.getValue()));
    loaded = persistenceService.find("*:*");
    assertThat(loaded, hasSize(1));
    assertThat(loaded, hasItems(testDesc));
  }

  @Test
  void whenRemoveByTypeIsCalledWithNotMatchingQuery_ThenNoEntityShouldbeRemoved() throws Exception {
    final HspBaseTestClass testObj = new HspBaseTestClass("testid", HspObjectType.OBJECT.getValue()[0]);
    persistenceService.add(testObj);

    final boolean removed = persistenceService.remove(String.format("%s:\"%s\"", "id-search", "invalid"));
    assertThat(removed, is(true));

    final Collection<HspBaseTestClass> loaded = persistenceService.find("*:*");
    assertThat(loaded, hasSize(1));
  }

  @Test
  void whenFindByQueryIsCalledWithMatchingQuery_ThenAllMatchingEntitiesShouldBeReturned() throws Exception {
    final HspBaseTestClass testObj = new HspBaseTestClass("testid", HspObjectType.OBJECT.getValue()[0]);
    final HspBaseTestClass testObj2 = new HspBaseTestClass("testid2", HspObjectType.OBJECT.getValue()[0]);
    final HspBaseTestClass testDesc = new HspBaseTestClass("testid3", HspObjectType.DESCRIPTION.getValue()[0]);

    persistenceService.addAll(List.of(testObj, testObj2, testDesc));

    Collection<HspBaseTestClass> loaded = persistenceService.find(Constants.FIELD_NAME_ID + ":" + "testid3");
    assertThat(loaded, hasSize(1));
  }

  @Test
  void whenFindByQueryIsCalledWithNoneMatchingQuery_ThenNoEntitiesShouldBeReturned() throws Exception {
    final HspBaseTestClass testObj = new HspBaseTestClass("testid", HspObjectType.OBJECT.getValue()[0]);
    final HspBaseTestClass testObj2 = new HspBaseTestClass("testid2", HspObjectType.OBJECT.getValue()[0]);
    final HspBaseTestClass testDesc = new HspBaseTestClass("testid3", HspObjectType.DESCRIPTION.getValue()[0]);

    persistenceService.addAll(List.of(testObj, testObj2, testDesc));

    Collection<HspBaseTestClass> loaded = persistenceService.find(String.format("%s:%s AND %s:\"%s\"", Constants.FIELD_NAME_ID, "testid3", Constants.FIELD_NAME_TYPE, HspObjectType.OBJECT.getValue()));
    assertThat(loaded, hasSize(0));
  }
}
