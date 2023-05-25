package de.staatsbibliothek.berlin.hsp.fo.indexer.service;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspBase;

import java.util.Collection;
import java.util.Optional;

/**
 * @param <T> the entity type this service should work with
 */

public interface EntitiyService<T extends HspBase> {

  /**
   * Finds an entity matching with a particular id
   *
   * @param id the Id that the entity should have
   * @return an {@link Optional} containing the matching entity, an empty {@code Optional} otherwise
   * @throws PersistenceServiceException
   */
  Optional<T> findById(final String id) throws PersistenceServiceException;

  /**
   * Saves an entity
   *
   * @param entity the entity to save
   * @return true if saving was sucessfull, false otherwise
   * @throws PersistenceServiceException
   */
  boolean save(final T entity) throws PersistenceServiceException;

  /**
   * Saves a {@link Collection} of entities
   *
   * @param entities the {@code Collection} of entities to save
   * @return true if saving was successful, false otherwise
   * @throws PersistenceServiceException
   */
  boolean saveAll(final Collection<T> entities) throws PersistenceServiceException;

  /**
   * Deletes all entities having the given {@code groupId}
   *
   * @param groupId the groupId that all entities that should be deleted match
   *                with
   * @throws PersistenceServiceException
   */
  boolean deleteByGroupId(final String groupId) throws PersistenceServiceException;

  /**
   * Deletes all entities. This usually is limited to entities of type <T>
   *
   * @throws PersistenceServiceException
   */
  void deleteAll() throws PersistenceServiceException;
}
