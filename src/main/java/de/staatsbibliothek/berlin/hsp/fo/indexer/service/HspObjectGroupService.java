package de.staatsbibliothek.berlin.hsp.fo.indexer.service;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspObjectGroup;

public interface HspObjectGroupService {
    void insert(HspObjectGroup hspObjectGroup) throws PersistenceServiceException;

    void insertOrUpdate(HspObjectGroup hspObjectGroup) throws PersistenceServiceException;

    void deleteByGroupId(String groupId) throws PersistenceServiceException;

    void deleteAll() throws PersistenceServiceException;

    void save(HspObjectGroup hspObjectGroup) throws PersistenceServiceException;
}
