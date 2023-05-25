package de.staatsbibliothek.berlin.hsp.fo.indexer.persistence;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspBase;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

/**
 * @param <T> the entity type the repository works with
 */
public interface Repository<T extends HspBase> {

  boolean deleteByGroupId(String groupId) throws PersistenceServiceException;

  /**
   * Saves the given entity
   *
   * @param entity to save
   * @return true if saving was successful, false otherwise
   * @throws PersistenceServiceException
   */
  boolean save(T entity) throws PersistenceServiceException;

  /**
   * Saves the given collection
   *
   * @param entities to save
   * @return true if saving was successful, false otherwise
   * @throws PersistenceServiceException
   */
  boolean saveAll(Collection<T> entities) throws PersistenceServiceException;

  /**
   * Performs a query based on the given {@code Id}
   *
   * @param Id the id to query for
   * @return An {@link Optional} containing the matching entity, an empty {@code Optional} otherwise
   * @throws PersistenceServiceException
   */
  Optional<T> findById(final String Id) throws IOException, PersistenceServiceException;

  /**
   * Performs a query based on the given groupId
   *
   * @param groupId the {@code groupId} to look for
   * @return a {@link Collection} containing all matching entities
   * @throws PersistenceServiceException
   */
  Collection<T> findByGroupId(String groupId) throws IOException, PersistenceServiceException;

  /**
   * Removes all entities matching the given {@code query}
   *
   * @param query the query expressing what entities to delete
   * @return true of deletion was successful, false otherwise
   * @throws PersistenceServiceException
   */
  boolean delete(final String query) throws PersistenceServiceException;

  /**
   * Removes all entities. The implementation may use a type filter to guarantee
   * that only items of type {@code T} are removed.
   *
   * @return true of deletion was successful, false otherwise
   * @throws PersistenceServiceException
   */
  boolean deleteAll() throws PersistenceServiceException;
}