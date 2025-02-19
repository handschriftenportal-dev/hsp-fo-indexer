package de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.Constants;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspBase;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.PersistenceService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.Repository;
import de.staatsbibliothek.berlin.hsp.fo.indexer.type.HspObjectType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * provides a basic abstraction for performing CRUD operations by using an instance of {@link PersistenceService}
 *
 * @param <T> The generic type the repository should work with
 */
public class RepositoryImpl<T extends HspBase> implements Repository<T> {
  private final String typeFilter;
  private static final String AND = " AND ";
  PersistenceService<T> persistenceService;

  public RepositoryImpl(@Autowired final PersistenceService<T> persistenceService, HspObjectType type) {
    this.persistenceService = persistenceService;
    this.typeFilter = Arrays.stream(type.getValue())
        .map(t -> String.format("%s:\"%s\"", Constants.FIELD_NAME_TYPE, t))
        .collect(Collectors.joining(" OR ", "(", ")"));
  }

  @Override
  public boolean save(final T value) throws PersistenceServiceException {
    return persistenceService.add(value);
  }

  @Override
  public boolean saveAll(final Collection<T> values) throws PersistenceServiceException {
    return persistenceService.addAll(values);
  }

  @Override
  public Optional<T> findById(final String id) throws PersistenceServiceException {
    final String query = Constants.FIELD_NAME_ID + ":" + id + AND + typeFilter;
    final Collection<T> results = persistenceService.find(query);
    return results.stream()
        .findFirst();
  }

  @Override
  public boolean delete(final String query) throws PersistenceServiceException {
    return persistenceService.remove(query + AND + typeFilter);
  }

  @Override
  public boolean deleteById(String id) throws PersistenceServiceException {
    return this.persistenceService.remove(Constants.FIELD_NAME_ID + ":" + id + AND + typeFilter);
  }

  @Override
  public boolean deleteAll() throws PersistenceServiceException {
    return persistenceService.remove(typeFilter);
  }
}
