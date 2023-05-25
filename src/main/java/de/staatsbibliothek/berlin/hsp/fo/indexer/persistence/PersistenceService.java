package de.staatsbibliothek.berlin.hsp.fo.indexer.persistence;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;

import java.util.Collection;

/**
 * @param <T> the entity type the service should handle
 */

public interface PersistenceService<T> {

  /**
   * Adds the given {@code entity}
   *
   * @param entity the entity to add
   * @return true if adding was successful, false otherwise
   * @throws PersistenceServiceException
   */
  boolean add(final T entity) throws PersistenceServiceException;

  /**
   * Adds a {@link Collection} of entities
   *
   * @param entities the {@code Collection} of entities to save
   * @return true if adding was successful, false otherwise
   * @throws PersistenceServiceException
   */
  boolean addAll(final Collection<T> entities) throws PersistenceServiceException;

  /**
   * Removes one or more entities
   *
   * @param query the query expressing what entities to delete
   * @return true if removal was successful, false otherwise
   * @throws PersistenceServiceException
   */
  boolean remove(final String query) throws PersistenceServiceException;

  /**
   * Finds all entities matching the given {@code query}
   *
   * @param query the query expressing what entities should be found
   * @return a {@link Collection} of all matching entities
   */
  Collection<T> find(final String query) throws PersistenceServiceException;
}
