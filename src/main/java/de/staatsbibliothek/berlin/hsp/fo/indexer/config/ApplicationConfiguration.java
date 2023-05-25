package de.staatsbibliothek.berlin.hsp.fo.indexer.config;

import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspDigitized;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspObject;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.SchemaVersion;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.PersistenceServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.RepositoryImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.SchemaVersionRepositoryImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.EntitiyService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.impl.EntityServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.type.HspObjectType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient.Builder;
import org.springframework.beans.factory.annotation.Value;
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
  public SolrClient solrClient(@Value("${solr.host}") String solrHost) {
    final String solrUrl = String.format("%s/solr", solrHost);
    return new Builder().withBaseSolrUrl(solrUrl)
        .withHttpClient(HttpClientBuilder.create()
            .build())
        .build();
  }

  @Bean
  @LoadBalanced
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }

  @Bean
  PersistenceServiceImpl<HspObject> objectPersistenceService(final SolrClient solrClient) {
    final PersistenceServiceImpl<HspObject> sps = new PersistenceServiceImpl<>(HspObject.class);
    sps.setSolrClient(solrClient);
    return sps;
  }

  @Bean
  PersistenceServiceImpl<HspDescription> descriptionPersistenceService(final SolrClient solrClient) {
    final PersistenceServiceImpl<HspDescription> sps = new PersistenceServiceImpl<>(HspDescription.class);
    sps.setSolrClient(solrClient);
    return sps;
  }

  @Bean
  PersistenceServiceImpl<HspDigitized> digitizationPersistenceService(final SolrClient solrClient) {
    final PersistenceServiceImpl<HspDigitized> sps = new PersistenceServiceImpl<>(HspDigitized.class);
    sps.setSolrClient(solrClient);
    return sps;
  }

  @Bean
  PersistenceServiceImpl<SchemaVersion> schemaVersionPersistenceService(final SolrClient solrClient) {
    final PersistenceServiceImpl<SchemaVersion> sps = new PersistenceServiceImpl<>(SchemaVersion.class);
    sps.setSolrClient(solrClient);
    return sps;
  }

  @Bean
  RepositoryImpl<HspObject> hspObjectSolrRepository(final PersistenceServiceImpl<HspObject> persistenceService) {
    return new RepositoryImpl<>(persistenceService, HspObjectType.OBJECT);
  }

  @Bean
  RepositoryImpl<HspDescription> hspDescriptionRepository(final PersistenceServiceImpl<HspDescription> sps) {
    return new RepositoryImpl<>(sps, HspObjectType.DESCRIPTION);
  }

  @Bean
  RepositoryImpl<HspDigitized> hspDigitizedSolrRepository(final PersistenceServiceImpl<HspDigitized> sps) {
    return new RepositoryImpl<>(sps, HspObjectType.DIGITIZATION);
  }

  @Bean
  SchemaVersionRepositoryImpl schemaVersionSolrRepository(final PersistenceServiceImpl<SchemaVersion> sps) {
    return new SchemaVersionRepositoryImpl(sps);
  }

  @Bean
  EntitiyService<HspObject> hspObjectService(final RepositoryImpl<HspObject> hspObjectRepository) {
    return new EntityServiceImpl<>(hspObjectRepository);
  }

  @Bean
  EntitiyService<HspDescription> hspDescriptionService(final RepositoryImpl<HspDescription> hspDescriptionRepository) {
    return new EntityServiceImpl<>(hspDescriptionRepository);
  }

  @Bean
  EntitiyService<HspDigitized> hspDigitizedService(final RepositoryImpl<HspDigitized> hspDigitizedRepository) {
    return new EntityServiceImpl<>(hspDigitizedRepository);
  }
}
