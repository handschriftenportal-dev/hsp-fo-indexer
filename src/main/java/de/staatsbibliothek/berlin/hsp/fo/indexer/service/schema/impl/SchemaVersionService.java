package de.staatsbibliothek.berlin.hsp.fo.indexer.service.schema.impl;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.SchemaVersion;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface SchemaVersionService {

  /**
   * get the schema version, if there is one
   *
   * @return an {@link Optional} containing the schema version, an empty {@code Optional} otherwise
   */
  Optional<SchemaVersion> find() throws PersistenceServiceException;

  /**
   * save (create or update) the schema version
   *
   * @param version the entity to save
   * @return true if saving was successful, false otherwise
   * @throws PersistenceServiceException
   */
  boolean save(final SchemaVersion version) throws PersistenceServiceException;

  /**
   * removes the document that holds the schema version info
   *
   * @return true if removal was successful, false otherwise
   * @throws PersistenceServiceException
   */
  boolean remove() throws PersistenceServiceException;
}
