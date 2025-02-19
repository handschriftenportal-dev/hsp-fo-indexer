package de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dreamhead.moco.HttpServer;
import com.github.dreamhead.moco.Runner;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspCatalog;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspDigitized;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspObject;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.SchemaVersion;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.nachweis.NachweisHttpAdapter;
import de.staatsbibliothek.berlin.hsp.fo.indexer.type.HspObjectType;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.RepositoryImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.SchemaVersionRepositoryImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.PersistenceServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.entity.impl.HspObjectGroupService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.schema.SchemaVersionServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.EmbeddedSolrServer;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.extension.EmbeddedSolr;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import static com.github.dreamhead.moco.Moco.pathResource;
import static com.github.dreamhead.moco.MocoJsonRunner.jsonHttpServer;

@ActiveProfiles("integration")
@TestConfiguration
@ContextConfiguration(classes = {
        HspObjectGroupService.class,
        NachweisHttpAdapter.class,
        SchemaVersionServiceImpl.class
})
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class ApplicationTestConfiguration {

  public static final String CONF_PATH = "solr";

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean(name = "embSolrExt")
  public EmbeddedSolr embeddedSolr() {
    return new EmbeddedSolr(new ClassPathResource(CONF_PATH));
  }

  @Bean(name = "embeddedSolrServer")
  public EmbeddedSolrServer embeddedSolrServer(final EmbeddedSolr embeddedSolr) throws Exception {
    embeddedSolr.prepare();
    return embeddedSolr.getSolrServer();
  }

  @Bean
  @Qualifier("leaderClient")
  public SolrClient solrClient(final EmbeddedSolrServer embeddedSolrServer) {
    return embeddedSolrServer.getSolrClient("hsp");
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
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
  PersistenceServiceImpl<HspCatalog> catalogPersistenceService(final SolrClient solrClient) {
    final PersistenceServiceImpl<HspCatalog> sps = new PersistenceServiceImpl<>(HspCatalog.class);
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
  RepositoryImpl<HspCatalog> hspCatalogSolrRepository(final PersistenceServiceImpl<HspCatalog> sps) {
    return new RepositoryImpl<>(sps, HspObjectType.DIGITIZATION);
  }

  @Bean
  SchemaVersionRepositoryImpl schemaVersionSolrRepository(final PersistenceServiceImpl<SchemaVersion> sps) {
    return new SchemaVersionRepositoryImpl(sps);
  }

  @Bean(name = "runner")
  Runner runner() {
    HttpServer server = jsonHttpServer(56789, pathResource("moco/nachweis.json"));
    Runner runner = Runner.runner(server);
    runner.start();
    return runner;
  }

  @Bean
  RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
