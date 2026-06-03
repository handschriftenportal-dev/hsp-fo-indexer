package de.staatsbibliothek.berlin.hsp.fo.indexer.persistence;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.SchemaVersion;

import java.util.Optional;

public interface SchemaVersionRepository {
    Optional<SchemaVersion> findById(String id) throws PersistenceServiceException;

    boolean save(SchemaVersion version) throws PersistenceServiceException;

    boolean remove(String id) throws PersistenceServiceException;
}
