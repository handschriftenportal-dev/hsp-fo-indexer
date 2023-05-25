package de.staatsbibliothek.berlin.hsp.fo.indexer.service.impl;

import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspDigitized;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspObject;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.EntitiyService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.HspObjectGroupService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HspObjectGroupServiceImpl implements HspObjectGroupService {

  private static final Logger logger = LoggerFactory.getLogger(HspObjectGroupServiceImpl.class);
  private final EntitiyService<HspObject> hspObjectService;
  private final EntitiyService<HspDescription> hspDescriptionService;
  private final EntitiyService<HspDigitized> hspDigitizedService;

  public HspObjectGroupServiceImpl(
      @Autowired final EntitiyService<HspObject> hspObjectService,
      @Autowired final EntitiyService<HspDescription> hspDescriptionService,
      @Autowired final EntitiyService<HspDigitized> hspDigitizedService) {
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
      final String id = hspObjectGroup.getHspObject()
          .getId();

      if (hspObjectService.findById(id)
          .isEmpty()) {
        save(hspObjectGroup);
      } else {
        logger.warn("Cannot insert hsp:object with id {} because it's already there.", id);
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
   * Removes all entities havong the given {@code groupId}
   *
   * @param groupId the groupId that all entities that should be deleted match
   *                with
   * @throws PersistenceServiceException
   */
  @Override
  public void deleteByGroupId(final String groupId) throws PersistenceServiceException {
    if (logger.isDebugEnabled()) {
      logger.debug("removing objects with groupId: {}", groupId);
    }
    hspObjectService.deleteByGroupId(groupId);
    hspDescriptionService.deleteByGroupId(groupId);
    hspDigitizedService.deleteByGroupId(groupId);
  }

  @Override
  public void deleteAll() throws PersistenceServiceException {
    hspObjectService.deleteAll();
    hspDescriptionService.deleteAll();
    hspDigitizedService.deleteAll();
  }

  @Override
  public void save(final HspObjectGroup hspObjectGroup) throws PersistenceServiceException {
    if (logger.isDebugEnabled()) {
      logger.debug("inserting object group: {}", hspObjectGroup);
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
