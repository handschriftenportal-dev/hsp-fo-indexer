package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.kafka;

import de.staatsbibliothek.berlin.hsp.fo.indexer.api.IndexerHealthIndicator;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.ReplicationException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.util.ActivityStreamMessageHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.SolrMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ContentResolverException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspBase;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspCatalog;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.entity.RepositoryService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.replication.ReplicationAdminService;
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
  private final RepositoryService<? extends HspBase> hspObjectGroupService;
  private final RepositoryService<? extends HspBase> hspCatalogService;
  private final SolrMapper solrMapper;
  private final ReplicationAdminService replicationAdminService;

  public SolrKafkaMessageHandler(
      @Autowired final SolrMapper solrMapper,
      @Autowired final RepositoryService<HspObjectGroup> hspObjectGroupService,
      @Autowired final RepositoryService<HspCatalog> hspCatalogService,
      @Autowired final ReplicationAdminService replicationAdminService) {
    this.solrMapper = solrMapper;
    this.hspObjectGroupService = hspObjectGroupService;
    this.hspCatalogService = hspCatalogService;
    this.replicationAdminService = replicationAdminService;
  }

  @Override
  public void handleMessage(final ActivityStreamMessage message) {
    log.info("start handling {} message containing objects: ", message.getAction());
    message.getObjects().forEach(ao -> log.info("type: {}, id: {}", ao.getType(), ao.getId()));

    final Optional<ActivityStreamsDokumentTyp> messageType = ActivityStreamMessageHelper.determineType(message);
    if(messageType.isEmpty()) {
      log.warn("Type ActivityStreamMessage could not be determined, will skip message");
      return;
    }

    final Optional<RepositoryService<? extends HspBase>> persistenceService = getServiceByType(messageType.get());
    if(persistenceService.isEmpty()) {
      log.warn("Not able to handle the given type of message ({})", messageType);
      return;
    }

    final Optional<? extends HspBase> entity = getEntityByType(message, messageType.get());
    if(entity.isEmpty()) {
      log.warn("Not able to determine the entity's type", messageType);
      return;
    }

    try {
      switch (message.getAction()) {
        case ADD:
          handleAddAction(entity.get(), (RepositoryService<HspBase>) persistenceService.get());
          break;
        case UPDATE:
          handleUpdateAction(entity.get(), (RepositoryService<HspBase>) persistenceService.get());
          break;
        case REMOVE:
          handleRemoveAction(entity.get(), (RepositoryService<HspBase>) persistenceService.get());
          break;
        case REMOVE_ALL:
          handleRemoveAll(messageType.get());
      }
    } catch (PersistenceServiceException | ReplicationException e) {
      if (e.isCritical()) {
        log.error("An exception occurred while handling message with id {}:", message.getId(), e);
        IndexerHealthIndicator.setCriticalException(e);
      } else {
        log.info("An exception occurred while handling message with id {}:", message.getId(), e.getMessage());
      }
    }
  }

  private void handleAddAction(final HspBase base, final RepositoryService<HspBase> service) throws PersistenceServiceException {
    service.insert(base);
  }

  private void handleUpdateAction(final HspBase base, final RepositoryService<HspBase> service) throws PersistenceServiceException {
    handleRemoveAction(base, service);
    service.insertOrUpdate(base);
  }

  private void handleRemoveAction(final HspBase base, final RepositoryService<HspBase> service) throws PersistenceServiceException {
    if (base != null && !StringUtils.isBlank(base.getId())) {
      service.deleteById(base.getId());
    }
  }

  private void handleRemoveAll(final ActivityStreamsDokumentTyp messageType) throws PersistenceServiceException, ReplicationException {
    replicationAdminService.disablePolling();

    if (ActivityStreamsDokumentTyp.ANY.equals(messageType)) {
      hspObjectGroupService.deleteAll();
      hspCatalogService.deleteAll();
    }
  }

  private Optional<byte[]> getContent(final ActivityStreamMessage activityStreamMessage, final ActivityStreamsDokumentTyp activityStreamsDokumentTyp) {
    final List<ActivityStreamObject> activityStreamObjects = ActivityStreamMessageHelper.getActivityStreamObjectsByType(activityStreamMessage, activityStreamsDokumentTyp);
    final byte[] content;
    if (activityStreamObjects.isEmpty()) {
      log.error("ActivityStreamMessage doesn't contain any objects. ASM id: {}", activityStreamMessage.getId());
      return Optional.empty();
    }
    if (activityStreamObjects.size() > 1) {
      log.error("ActivityStreamMessage contains multiple objects. ASM id: {}", activityStreamMessage.getId());
      return Optional.empty();
    }

    try {
      content = activityStreamObjects.getFirst().getContent();
    } catch (ActivityStreamsException e) {
      log.error("Error while getting content of ASM's KOD. ASM ID: {}", activityStreamMessage.getId());
      return Optional.empty();
    }
    return Optional.of(content);
  }

  private List<byte[]> getDescriptionContents(final ActivityStreamMessage activityStreamMessage) {
    final List<ActivityStreamObject> descriptionObjects = ActivityStreamMessageHelper.getActivityStreamObjectsByType(activityStreamMessage, ActivityStreamsDokumentTyp.BESCHREIBUNG);
    final List<byte[]> result = new ArrayList<>();
    for (ActivityStreamObject aso : descriptionObjects) {
      try {
        result.add(aso.getContent());
      } catch (ActivityStreamsException e) {
        log.warn("Error while getting content of ASM's KOD. ASM ID: {}", activityStreamMessage.getId());
      }
    }
    return result;
  }

  /**
   * Determines an appropriate implementation of {@link RepositoryService} based on the given {@link ActivityStreamsDokumentTyp}
   * @param messageType the type of the message, which hints to its content
   * @return an {@link Optional} containing an implementation of {@link RepositoryService}, if there is one available for the given type, empty {@code Optional} otherwise
   */
  private Optional<RepositoryService<? extends HspBase>> getServiceByType(final ActivityStreamsDokumentTyp messageType) {
    final RepositoryService<? extends HspBase> result;
    switch (messageType) {
      case KOD -> result = hspObjectGroupService;
      case KATALOG -> result = hspCatalogService;
      default -> result = null;
    }

    return Optional.ofNullable(result);
  }

  private Optional<? extends HspBase> getEntityByType(final ActivityStreamMessage message, final ActivityStreamsDokumentTyp messageType) {
    final Optional<byte[]> tei = getContent(message, messageType);
    Optional<? extends HspBase> entity = Optional.empty();
    try {
      switch (messageType) {
        case KOD -> entity = solrMapper.mapHspObjectGroup(tei.orElse(null), getDescriptionContents(message));
        case KATALOG -> entity = solrMapper.mapHspCatalog(tei.orElse(null));
      }
    } catch(ContentResolverException e) {
      log.warn("An error occurred while mapping the content of message with id {}", message.getId(), e);
    }
    return entity;
  }
}
