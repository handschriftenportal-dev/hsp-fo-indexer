package de.staatsbibliothek.berlin.hsp.fo.indexer.service.entity.impl;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.Constants;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.*;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.RepositoryImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.entity.RepositoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HspObjectGroupService implements RepositoryService<HspObjectGroup>  {
  private final RepositoryImpl<HspObject> hspObjectService;
  private final RepositoryImpl<HspDescription> hspDescriptionService;
  private final RepositoryImpl<HspDigitized> hspDigitizedService;

  public HspObjectGroupService(
      @Autowired final RepositoryImpl<HspObject> hspObjectService,
      @Autowired final RepositoryImpl<HspDescription> hspDescriptionService,
      @Autowired final RepositoryImpl<HspDigitized> hspDigitizedService) {
    this.hspObjectService = hspObjectService;
    this.hspDescriptionService = hspDescriptionService;
    this.hspDigitizedService = hspDigitizedService;
  }

  /**
   * Inserts a {@link HspObjectGroup} instance
   *
   * @param hspObjectGroup the {@code HspObjectGroup} instance to insert
   */
  @Override
  public void insert(final HspObjectGroup hspObjectGroup) throws PersistenceServiceException {
    if (hspObjectGroup != null && hspObjectGroup.getHspObject() != null) {
      final String id = hspObjectGroup.getHspObject().getId();

      if (hspObjectService.findById(id).isEmpty()) {
        save(hspObjectGroup);
      } else {
        log.warn("Cannot insert hsp:object with id {} because it's already there.", id);
      }
    }
  }

  /**
   * inserts a {@link HspObjectGroup}. Unlike {@link #insert(HspObjectGroup)}
   * this will replace an existing {@code HspObjectGroup}
   *
   * @param hspObjectGroup the {@code HspObjectGroup} instance to insert or
   *                       update
   * @throws PersistenceServiceException
   */
  @Override
  public void insertOrUpdate(final HspObjectGroup hspObjectGroup) throws PersistenceServiceException {
    if (hspObjectGroup != null && hspObjectGroup.getHspObject() != null) {
      save(hspObjectGroup);
    }
  }

  /**
   * Removes all entities having the given {@code groupId}
   *
   * @param id the groupId that all entities that should be deleted match
   *                with
   * @throws PersistenceServiceException
   */
  @Override
  public void deleteById(final String id) throws PersistenceServiceException {
    if (log.isDebugEnabled()) {
      log.debug("removing objects with groupId: {}", id);
    }
    hspObjectService.delete(Constants.FIELD_NAME_GROUP_ID + ":" + id);
    hspDescriptionService.delete(Constants.FIELD_NAME_GROUP_ID + ":" + id);
    hspDigitizedService.delete(Constants.FIELD_NAME_GROUP_ID + ":" + id);
  }

  @Override
  public void deleteAll() throws PersistenceServiceException {
    hspObjectService.deleteAll();
    hspDescriptionService.deleteAll();
    hspDigitizedService.deleteAll();
  }

  private void save(final HspObjectGroup hspObjectGroup) throws PersistenceServiceException {
    if (log.isDebugEnabled()) {
      log.debug("inserting object group: {}", hspObjectGroup);
    }
    hspObjectService.save(hspObjectGroup.getHspObject());
    if (CollectionUtils.isNotEmpty(hspObjectGroup.getHspDescriptions())) {
      hspDescriptionService.saveAll(hspObjectGroup.getHspDescriptions());
    }
    if (CollectionUtils.isNotEmpty(hspObjectGroup.getHspDigitized())) {
      hspDigitizedService.saveAll(hspObjectGroup.getHspDigitized());
    }
  }
}
