package de.staatsbibliothek.berlin.hsp.fo.indexer.config;

import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspCatalog;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspDigitized;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspObject;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.SchemaVersion;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.PersistenceServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.RepositoryImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.SchemaVersionRepositoryImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.type.HspObjectType;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 *
 */
@Configuration
@ComponentScan
@EnableScheduling
public class ApplicationConfiguration {

  @Bean
  @LoadBalanced
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }

  @Bean
  PersistenceServiceImpl<HspObject> objectPersistenceService(final SolrClient solrClient) {
    final PersistenceServiceImpl<HspObject> persistenceService = new PersistenceServiceImpl<>(HspObject.class);
    persistenceService.setSolrClient(solrClient);
    return persistenceService;
  }

  @Bean
  PersistenceServiceImpl<HspDescription> descriptionPersistenceService(final SolrClient solrClient) {
    final PersistenceServiceImpl<HspDescription> persistenceService = new PersistenceServiceImpl<>(HspDescription.class);
    persistenceService.setSolrClient(solrClient);
    return persistenceService;
  }

  @Bean
  PersistenceServiceImpl<HspDigitized> digitizationPersistenceService(final SolrClient solrClient) {
    final PersistenceServiceImpl<HspDigitized> persistenceService = new PersistenceServiceImpl<>(HspDigitized.class);
    persistenceService.setSolrClient(solrClient);
    return persistenceService;
  }

  @Bean
  PersistenceServiceImpl<SchemaVersion> schemaVersionPersistenceService(final SolrClient solrClient) {
    final PersistenceServiceImpl<SchemaVersion> persistenceService = new PersistenceServiceImpl<>(SchemaVersion.class);
    persistenceService.setSolrClient(solrClient);
    return persistenceService;
  }

  @Bean
  PersistenceServiceImpl<HspCatalog> catalogPersistenceService(final SolrClient solrClient) {
    final PersistenceServiceImpl<HspCatalog> persistenceService = new PersistenceServiceImpl<>(HspCatalog.class);
    persistenceService.setSolrClient(solrClient);
    return persistenceService;
  }

  @Bean
  RepositoryImpl<HspObject> hspObjectRepository(final PersistenceServiceImpl<HspObject> persistenceService) {
    return new RepositoryImpl<>(persistenceService, HspObjectType.OBJECT);
  }

  @Bean
  RepositoryImpl<HspDescription> hspDescriptionRepository(final PersistenceServiceImpl<HspDescription> sps) {
    return new RepositoryImpl<>(sps, HspObjectType.DESCRIPTION);
  }

  @Bean
  RepositoryImpl<HspDigitized> hspDigitizedRepository(final PersistenceServiceImpl<HspDigitized> sps) {
    return new RepositoryImpl<>(sps, HspObjectType.DIGITIZATION);
  }

  @Bean
  SchemaVersionRepositoryImpl schemaVersionSolrRepository(final PersistenceServiceImpl<SchemaVersion> sps) {
    return new SchemaVersionRepositoryImpl(sps);
  }

  @Bean
  RepositoryImpl<HspCatalog> hspCatalogRepository(final PersistenceServiceImpl<HspCatalog> ps) {
    return new RepositoryImpl<>(ps, HspObjectType.CATALOG);
  }
}
