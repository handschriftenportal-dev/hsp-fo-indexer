package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.kafka;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ContentResolverException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.model.ActivityStreamMessage;

/**
 *
 */
public interface IKafkaMessageHandler {
  void handleMessage(ActivityStreamMessage message) throws PersistenceServiceException, ContentResolverException;
}
