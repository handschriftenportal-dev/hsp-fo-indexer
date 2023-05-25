package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.kafka;

import de.staatsbibliothek.berlin.hsp.fo.indexer.api.IndexerHealthIndicator;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.util.ActivityStreamMessageHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.SolrMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ContentResolverException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.HspObjectGroupService;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.model.ActivityStreamMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 */
@Component
@Slf4j
public class SolrKafkaMessageHandler implements IKafkaMessageHandler {
  private final HspObjectGroupService hspObjectGroupService;
  private final SolrMapper solrMapper;

  public SolrKafkaMessageHandler(
      @Autowired final SolrMapper solrMapper,
      @Autowired final HspObjectGroupService hspObjectGroupService) {
    this.solrMapper = solrMapper;
    this.hspObjectGroupService = hspObjectGroupService;
  }

  @Override
  public void handleMessage(final ActivityStreamMessage message) {
    log.info("start handling {} message containing objects: ", message.getAction());
    message.getObjects().forEach(ao -> log.info("type: {}, id: {}", ao.getType(), ao.getId()));

    try {
      final Optional<byte[]> kodContent = getKODContent(message);

      final HspObjectGroup hspObjectGroup = solrMapper.mapHspObjectGroup(kodContent.orElse(null), getDescriptionContents(message));

      switch (message.getAction()) {
        case ADD:
          handleAddAction(hspObjectGroup);
          break;
        case UPDATE:
          handleUpdateAction(hspObjectGroup);
          break;
        case REMOVE:
          handleRemoveAction(hspObjectGroup);
          break;
      }

    } catch (ContentResolverException | PersistenceServiceException e) {
      log.warn("An exception occurred while handling message with id {}: {}", message.getId(), e);
      if (e.isSerious()) {
        IndexerHealthIndicator.setUnhealthyException(e);
      }
    }
  }

  private void handleAddAction(final HspObjectGroup hspObjectGroup) throws PersistenceServiceException {
    if (hspObjectGroup != null) {
      hspObjectGroupService.insert(hspObjectGroup);
    }
  }

  private void handleUpdateAction(final HspObjectGroup hspObjectGroup) throws PersistenceServiceException {
    handleRemoveAction(hspObjectGroup);
    hspObjectGroupService.insertOrUpdate(hspObjectGroup);
  }

  private void handleRemoveAction(final HspObjectGroup hspObjectGroup) throws PersistenceServiceException {
    if (hspObjectGroup != null && hspObjectGroup.getHspObject() != null && !StringUtils.isBlank(hspObjectGroup.getHspObject()
        .getId())) {
      final String groupId = hspObjectGroup.getHspObject()
          .getId();
      hspObjectGroupService.deleteByGroupId(groupId);
    }
  }

  private Optional<byte[]> getKODContent(final ActivityStreamMessage activityStreamMessage) {
    final List<ActivityStreamObject> kods = ActivityStreamMessageHelper.getActivityStreamObjectsByType(activityStreamMessage, ActivityStreamsDokumentTyp.KOD);
    final byte[] kodContent;
    if(kods.isEmpty()) {
      log.error("ActivityStreamMessage doesn't contain a KOD. ASM id: {}", activityStreamMessage.getId());
      return Optional.empty();
    }
    if(kods.size() > 1) {
      log.error("ActivityStreamMessage contains multiple KODs. ASM id: {}", activityStreamMessage.getId());
      return Optional.empty();
    }

    try {
      kodContent = kods.get(0).getContent();
    } catch(ActivityStreamsException e) {
      log.error("Error while getting content of ASM's KOD. ASM ID: {}", activityStreamMessage.getId());
      return Optional.empty();
    }
    return Optional.of(kodContent);
  }

  private List<byte[]> getDescriptionContents(final ActivityStreamMessage activityStreamMessage) {
    final List<ActivityStreamObject> descriptionObjects =ActivityStreamMessageHelper.getActivityStreamObjectsByType(activityStreamMessage, ActivityStreamsDokumentTyp.BESCHREIBUNG);
    final List<byte[]> result = new ArrayList<>();
    for(ActivityStreamObject aso : descriptionObjects) {
      try {
        result.add(aso.getContent());
      } catch(ActivityStreamsException e) {
        log.warn("Error while getting content of ASM's KOD. ASM ID: {}", activityStreamMessage.getId());
      }
    }
    return result;
  }
}
