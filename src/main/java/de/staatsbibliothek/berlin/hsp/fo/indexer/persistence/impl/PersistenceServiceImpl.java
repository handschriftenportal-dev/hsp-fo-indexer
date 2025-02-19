package de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspBase;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.PersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.impl.BaseHttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
@Slf4j
public class PersistenceServiceImpl<T extends HspBase> implements PersistenceService<T> {
  final Class<T> clazz;
  private final DocumentObjectBinder binder;
  protected SolrClient solrClient;
  protected String collectionName;

  public PersistenceServiceImpl(final Class<T> clazz) {
    this.clazz = clazz;
    binder = new DocumentObjectBinder();
  }

  public void setSolrClient(@Autowired final SolrClient solrClient) {
    this.solrClient = solrClient;
  }

  @Autowired
  public void setCollectionName(@Value("${solr.core}") String collectionName) {
    this.collectionName = collectionName;
  }

  protected SolrParams buildSolrParams(final String query) {
    return new MapSolrParams(Map.of("q", query));
  }

  @Override
  public Collection<T> find(final String query) throws PersistenceServiceException {
    final SolrParams solrParams = buildSolrParams(query);
    final QueryResponse response;
    try {
      response = solrClient.query(collectionName, solrParams);
    } catch (IOException | SolrServerException e) {
      throw new PersistenceServiceException(e.getMessage(), e, true, true);
    }
    if (response != null) {
      if (response.getException() == null) {
        return binder.getBeans(clazz, response.getResults());
      } else {
        log.info("An error occurred while querying for {}: {}", query, response.getException()
            .getMessage(), response.getException());
      }
    }
    return Collections.emptyList();
  }

  @Override
  public boolean remove(final String query) throws PersistenceServiceException {
    final UpdateResponse response;
    try {
      response = solrClient.deleteByQuery(collectionName, query);
      solrClient.commit(collectionName);
    } catch (IOException | SolrServerException e) {
      throw new PersistenceServiceException(e.getMessage(), e, true, true);
    }

    if (response != null) {
      if (response.getException() == null) {
        return true;
      } else {
        log.info("An error occurred while removing with query {}: {}", query, response.getException()
            .getMessage(), response.getException());
      }
    }
    return false;
  }

  @Override
  public boolean add(final T entity) throws PersistenceServiceException {
    checkEntity(entity);
    final UpdateResponse response;
    try {
      response = solrClient.addBean(collectionName, entity);
      solrClient.commit(collectionName);
    }
      catch(BaseHttpSolrClient.RemoteSolrException e) {
      throw new PersistenceServiceException(e.getMessage(), e, false, false);
    } catch (IOException | SolrServerException e) {
      log.warn("error", e);
      throw new PersistenceServiceException(e.getMessage(), e, true, true);
    }

    if (response != null) {
      if (response.getException() == null) {
        return true;
      } else {
        log.info("An error occurred while saving entity: {}", response.getException()
            .getMessage(), response.getException());
      }
    }
    return false;
  }

  @Override
  public boolean addAll(final Collection<T> entities) throws PersistenceServiceException {
    checkEntity(entities);
    final UpdateResponse response;
    try {
      response = solrClient.addBeans(collectionName, entities);
      solrClient.commit(collectionName);
    } catch (IOException | SolrServerException | SolrException e) {
      throw new PersistenceServiceException(e.getMessage(), e, true, true);
    }

    if (response != null) {
      if (response.getException() == null) {
        return true;
      } else {
        log.info("An error occurred while saving entities: {}", response.getException()
            .getMessage(), response.getException());
      }
    }
    return false;
  }

  private static void checkEntity(final Object entity) throws PersistenceServiceException {
    if(Objects.isNull(entity)) {
      throw new PersistenceServiceException("Cannot save object because its value is null");
    }
  }
}
