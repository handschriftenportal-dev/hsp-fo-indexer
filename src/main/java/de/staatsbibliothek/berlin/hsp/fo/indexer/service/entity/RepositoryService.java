package de.staatsbibliothek.berlin.hsp.fo.indexer.service.entity;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspBase;

public interface RepositoryService<T extends HspBase> {
    void insert(T hspBase) throws PersistenceServiceException;

    void insertOrUpdate(T hspBase) throws PersistenceServiceException;

    void deleteById(String groupId) throws PersistenceServiceException;

    void deleteAll() throws PersistenceServiceException;
}
