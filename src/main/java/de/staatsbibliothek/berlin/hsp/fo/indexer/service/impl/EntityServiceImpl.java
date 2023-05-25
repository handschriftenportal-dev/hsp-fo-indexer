package de.staatsbibliothek.berlin.hsp.fo.indexer.service.impl;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspBase;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.RepositoryImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.EntitiyService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Optional;

/**
 *
 */
public class EntityServiceImpl<T extends HspBase> implements EntitiyService<T> {

  private final RepositoryImpl<T> solrRepository;

  public EntityServiceImpl(@Autowired final RepositoryImpl<T> repository) {
    this.solrRepository = repository;
  }

  @Override
  public boolean save(T entity) throws PersistenceServiceException {
    return solrRepository.save(entity);
  }

  @Override
  public boolean saveAll(Collection<T> entities) throws PersistenceServiceException {
    return solrRepository.saveAll(entities);
  }

  @Override
  public boolean deleteByGroupId(final String groupId) throws PersistenceServiceException {
    return solrRepository.deleteByGroupId(groupId);
  }

  @Override
  public void deleteAll() throws PersistenceServiceException {
    solrRepository.deleteAll();
  }

  @Override
  public Optional<T> findById(final String id) throws PersistenceServiceException {
    return solrRepository.findById(id);
  }
}
