package de.staatsbibliothek.berlin.hsp.fo.indexer.api;

import de.staatsbibliothek.berlin.hsp.fo.indexer.BaseIntegrationTest;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.HSPException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.config.WebApplicationTestConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonPartEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for IndexerHealthIndicator
 */

@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"management.port=0", "eureka.client.enabled=false"})
@ContextConfiguration(classes = {WebApplicationTestConfiguration.class, IndexerHealthIndicator.class})
class IndexerHealthIndicatorTest extends BaseIntegrationTest implements ApplicationContextAware {

  private ApplicationContext applicationContext;

  @Value("${local.management.port}")
  private int mgt;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @DirtiesContext
  @Test
  void whenSettingUnhealthyException_thenStatusIsDown() throws Exception {
    applicationContext.getBean(HealthContributorRegistry.class)
        .unregisterContributor("solr");
    HttpResponse response = Request.Get("http://localhost:" + this.mgt + "/api/health")
        .execute()
        .returnResponse();

    assertThat(response.getStatusLine()
        .getStatusCode(), is(HttpStatus.SC_OK));
    assertThat(EntityUtils.toString(response.getEntity()), jsonPartEquals("status", "UP"));

    IndexerHealthIndicator.setCriticalException(new HSPException("test"));

    response = Request.Get("http://localhost:" + this.mgt + "/api/health")
        .execute()
        .returnResponse();

    assertThat(response.getStatusLine()
        .getStatusCode(), is(HttpStatus.SC_SERVICE_UNAVAILABLE));
    assertThat(EntityUtils.toString(response.getEntity()), jsonPartEquals("status", "DOWN"));
  }
}
