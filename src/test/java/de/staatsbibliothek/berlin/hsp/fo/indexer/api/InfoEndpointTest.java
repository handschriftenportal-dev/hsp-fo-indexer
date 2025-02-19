package de.staatsbibliothek.berlin.hsp.fo.indexer.api;

import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.config.WebApplicationTestConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonPartEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"management.port=0"})
@ActiveProfiles("integration")
@ContextConfiguration(classes = {WebApplicationTestConfiguration.class})
class InfoEndpointTest {

  @LocalServerPort
  private int port;

  @Value("${local.management.port}")
  private int mgt;

  @Value("${info.version}")
  private String projectVersion;

  @Test
  void whenInfoEndpointIsCalled_resultContainsVersionInfo() throws Exception {

    HttpResponse response = Request.Get("http://localhost:" + this.port + "/api/info")
        .execute()
        .returnResponse();
    assertThat(response.getStatusLine()
        .getStatusCode(), is(org.apache.http.HttpStatus.SC_OK));
    assertThat(EntityUtils.toString(response.getEntity()), jsonPartEquals("version", projectVersion));
  }
}
