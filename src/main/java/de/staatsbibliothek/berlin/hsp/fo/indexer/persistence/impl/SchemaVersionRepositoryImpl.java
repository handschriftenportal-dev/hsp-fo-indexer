package de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.SchemaVersion;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.PersistenceService;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

/**
 * Repository providing CRU operations for manipulating {@link SchemaVersion}
 */
public class SchemaVersionRepositoryImpl implements de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.SchemaVersionRepository {

  private final PersistenceService<SchemaVersion> repository;

  public SchemaVersionRepositoryImpl(PersistenceService<SchemaVersion> repository) {
    this.repository = repository;
  }

  /**
   * queries for {@link SchemaVersion} matching the given id
   *
   * @param id the id to query for
   * @return An {@link Optional} containing the matching {@code SchemaVersion}, an empty {@code Optional} otherwise
   * @throws PersistenceServiceException wraps {@link IOException} and {@link @SolrServerException}
   */
  @Override
  public Optional<SchemaVersion> findById(final String id) throws PersistenceServiceException {
    Collection<SchemaVersion> result = repository.find("id:" + id);
    return result.stream()
        .findFirst();
  }

  /**
   * saves the given {@link SchemaVersion} instance
   *
   * @param version the entity to save
   * @return true if saving was successful, false otherwise
   * @throws PersistenceServiceException wraps {@link IOException} and {@link @SolrServerException}
   */
  @Override
  public boolean save(SchemaVersion version) throws PersistenceServiceException {
    return repository.add(version);
  }

  @Override
  public boolean remove(final String id) throws PersistenceServiceException {
    return repository.remove("id:" + id);
  }
}
