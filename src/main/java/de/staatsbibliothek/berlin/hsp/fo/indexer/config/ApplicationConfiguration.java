package de.staatsbibliothek.berlin.hsp.fo.indexer.config;

import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspCatalog;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspDigitized;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspObject;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.SchemaVersion;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.GenericRepositoryServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.HspObjectType;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.SchemaVersionRepositoryImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.SolrRepositoryImpl;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 *
 */
@Configuration
@ComponentScan
@EnableScheduling
@EnableRetry
public class ApplicationConfiguration {

  @Bean
  @LoadBalanced
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }

  @Bean
  SolrRepositoryImpl<HspObject> objectPersistenceService(final SolrClient solrClient) {
    final SolrRepositoryImpl<HspObject> persistenceService = new SolrRepositoryImpl<>(HspObject.class);
    persistenceService.setSolrClient(solrClient);
    return persistenceService;
  }

  @Bean
  SolrRepositoryImpl<HspDescription> descriptionPersistenceService(final SolrClient solrClient) {
    final SolrRepositoryImpl<HspDescription> persistenceService = new SolrRepositoryImpl<>(HspDescription.class);
    persistenceService.setSolrClient(solrClient);
    return persistenceService;
  }

  @Bean
  SolrRepositoryImpl<HspDigitized> digitizationPersistenceService(final SolrClient solrClient) {
    final SolrRepositoryImpl<HspDigitized> persistenceService = new SolrRepositoryImpl<>(HspDigitized.class);
    persistenceService.setSolrClient(solrClient);
    return persistenceService;
  }

  @Bean
  SolrRepositoryImpl<SchemaVersion> schemaVersionPersistenceService(final SolrClient solrClient) {
    final SolrRepositoryImpl<SchemaVersion> persistenceService = new SolrRepositoryImpl<>(SchemaVersion.class);
    persistenceService.setSolrClient(solrClient);
    return persistenceService;
  }

  @Bean
  SolrRepositoryImpl<HspCatalog> catalogPersistenceService(final SolrClient solrClient) {
    final SolrRepositoryImpl<HspCatalog> persistenceService = new SolrRepositoryImpl<>(HspCatalog.class);
    persistenceService.setSolrClient(solrClient);
    return persistenceService;
  }

  @Bean
  GenericRepositoryServiceImpl<HspObject> hspObjectRepository(final SolrRepositoryImpl<HspObject> persistenceService) {
    return new GenericRepositoryServiceImpl<>(persistenceService, HspObjectType.OBJECT);
  }

  @Bean
  GenericRepositoryServiceImpl<HspDescription> hspDescriptionRepository(final SolrRepositoryImpl<HspDescription> sps) {
    return new GenericRepositoryServiceImpl<>(sps, HspObjectType.DESCRIPTION);
  }

  @Bean
  GenericRepositoryServiceImpl<HspDigitized> hspDigitizedRepository(final SolrRepositoryImpl<HspDigitized> sps) {
    return new GenericRepositoryServiceImpl<>(sps, HspObjectType.DIGITIZATION);
  }

  @Bean
  SchemaVersionRepositoryImpl schemaVersionSolrRepository(final SolrRepositoryImpl<SchemaVersion> sps) {
    return new SchemaVersionRepositoryImpl(sps);
  }

  @Bean
  GenericRepositoryServiceImpl<HspCatalog> hspCatalogRepository(final SolrRepositoryImpl<HspCatalog> ps) {
    return new GenericRepositoryServiceImpl<>(ps, HspObjectType.CATALOG);
  }
}
