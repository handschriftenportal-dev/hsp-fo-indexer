package de.staatsbibliothek.berlin.hsp.fo.indexer.service;

import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.SchemaVersion;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.HSPException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.BaseSolrTest;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.SchemaVersionRepositoryImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.PersistenceServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.schema.SchemaVersionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * test class for SchemaVersionService
 */

class SchemaVersionServiceTest extends BaseSolrTest {

  private static SchemaVersionServiceImpl schemaVersionService;
  protected PersistenceServiceImpl<SchemaVersion> persistenceService;

  @BeforeEach
  public final void init() throws Exception {
    super.setUp();
    persistenceService = new PersistenceServiceImpl<>(SchemaVersion.class);
    persistenceService.setSolrClient(BaseSolrTest.embeddedSolr.getSolrServer()
        .getSolrClient("hsp"));
    persistenceService.setCollectionName("hsp");
    final SchemaVersionRepositoryImpl repository = new SchemaVersionRepositoryImpl(persistenceService);
    schemaVersionService = new SchemaVersionServiceImpl(repository);
  }

  @Test
  void whenIsCalledWithExpectedVersionLowerThanCurrentVersion_ThenFalseIsReturned() throws Exception {
    final boolean isUpToDate = SchemaVersionServiceImpl.isUpToDate("1.0", "2.0");

    assertThat(isUpToDate, is(false));
  }

  @Test
  void whenIsCalledWithExpectedVersionLowerThanCurrentVersion_ThenTrueIsReturned() throws Exception {
    final boolean isUpToDate = SchemaVersionServiceImpl.isUpToDate("1.0", "1.0");

    assertThat(isUpToDate, is(true));
  }

  @Test
  void whenIsCalledWithExpectedVersionHigherThanCurrentVersion_ThenExceptionIsThrown() {
    assertThrows(HSPException.class, () -> SchemaVersionServiceImpl.isUpToDate("2.0", "1.0"));
  }

  @Test
  void whenSaveSchemaVersionIsCalled_thenSchemaVersionShouldBeSaved() throws Exception {
    final SchemaVersion schemaVersion = new SchemaVersion("99.3");

    schemaVersionService.save(schemaVersion);

    final Optional<SchemaVersion> loadedSchemaVersion = schemaVersionService.find();
    assertThat(loadedSchemaVersion, isPresent());
    assertThat(loadedSchemaVersion.get()
        .getCurrentVersion(), is(schemaVersion.getCurrentVersion()));
  }

  @Test
  void whenRemoveSchemaVersionIsCalled_thenSchemaVersionShouldBeRemoved() throws Exception {
    final SchemaVersion schemaVersion = new SchemaVersion("99.3");
    schemaVersionService.save(schemaVersion);
    Optional<SchemaVersion> loadedSchemaVersion = schemaVersionService.find();
    assertThat(loadedSchemaVersion, isPresent());

    schemaVersionService.remove();
    loadedSchemaVersion = schemaVersionService.find();
    assertThat(loadedSchemaVersion, isEmpty());
  }
}