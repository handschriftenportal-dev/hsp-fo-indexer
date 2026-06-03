package de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.Constants;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspBase;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.GenericRepositoryService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * provides a basic abstraction for performing CRUD operations by using an instance of {@link Repository}
 *
 * @param <T> The generic type the repository should work with
 */
public class GenericRepositoryServiceImpl<T extends HspBase> implements GenericRepositoryService<T> {
  private final String typeFilter;
  private static final String AND = " AND ";
  Repository<T> repository;

  public GenericRepositoryServiceImpl(@Autowired final Repository<T> repository, HspObjectType type) {
    this.repository = repository;
    this.typeFilter = Arrays.stream(type.getValue())
        .map(t -> String.format("%s:\"%s\"", Constants.FIELD_NAME_TYPE, t))
        .collect(Collectors.joining(" OR ", "(", ")"));
  }

  @Override
  public boolean save(final T value) throws PersistenceServiceException {
    return repository.add(value);
  }

  @Override
  public boolean saveAll(final Collection<T> values) throws PersistenceServiceException {
    return repository.addAll(values);
  }

  @Override
  public Optional<T> findById(final String id) throws PersistenceServiceException {
    final String query = Constants.FIELD_NAME_ID + ":" + id + AND + typeFilter;
    final Collection<T> results = repository.find(query);
    return results.stream()
        .findFirst();
  }

  @Override
  public boolean delete(final String query) throws PersistenceServiceException {
    return repository.remove(query + AND + typeFilter);
  }

  @Override
  public boolean deleteById(String id) throws PersistenceServiceException {
    return this.repository.remove(Constants.FIELD_NAME_ID + ":" + id + AND + typeFilter);
  }

  @Override
  public boolean deleteAll() throws PersistenceServiceException {
    return repository.remove(typeFilter);
  }
}
