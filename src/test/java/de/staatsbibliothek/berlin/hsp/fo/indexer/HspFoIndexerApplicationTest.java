package de.staatsbibliothek.berlin.hsp.fo.indexer;

import com.github.dreamhead.moco.Runner;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.SchemaVersion;
import de.staatsbibliothek.berlin.hsp.fo.indexer.api.IndexerHealthIndicator;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.kafka.KafkaMessageReceiver;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.impl.HspObjectGroupServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.SchemaVersionService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.impl.SchemaServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.impl.SchemaVersionServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.config.ApplicationTestConfiguration;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.extension.EmbeddedSolr;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ActiveProfiles("integration")
@ContextConfiguration(classes = {ApplicationTestConfiguration.class, HspFoIndexerCommandLineRunner.class, HspObjectGroupServiceImpl.class, SchemaServiceImpl.class, SchemaVersionServiceImpl.class,})
@SpringBootTest
public class HspFoIndexerApplicationTest extends BaseIntegrationTest {

  @MockBean
  KafkaMessageReceiver kafkaMessageReceiver;

  @Autowired
  private SchemaVersionService schemaVersionService;

  @Autowired
  private ApplicationContext ctx;

  @Value("${solr.schema-version}")
  private String schemaVersion;

  public HspFoIndexerApplicationTest() {
    super();
  }

  @AfterEach
  public void tearDown() {
    ((EmbeddedSolr) this.ctx.getBean("embSolrExt")).cleanUp();
    ((Runner) this.ctx.getBean("runner")).stop();
  }

  @Test
  public void testSchemaUpdate() throws Exception {
    final Optional<SchemaVersion> meta = schemaVersionService.find();

    assertThat(true, is(IndexerHealthIndicator.isHealthy()));
    assertThat(meta, isPresent());
    assertThat(meta.get()
        .getCurrentVersion(), is(schemaVersion));
  }
}
