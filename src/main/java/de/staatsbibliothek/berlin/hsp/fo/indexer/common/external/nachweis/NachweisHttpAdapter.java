package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.nachweis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Allows requesting the NachweisService via HTTP
 */

@Component
public class NachweisHttpAdapter {

  private static final Logger logger = LoggerFactory.getLogger(NachweisHttpAdapter.class);

  private String nachweisServiceId;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  public void setNachweisServiceId(
      @Value("${nachweis.id}") final String nachweiseServiceId) {
    this.nachweisServiceId = nachweiseServiceId;
  }

  public void triggerIndexing() {
    final String url = String.format("http://%s/rest/indexkods", this.nachweisServiceId);
    logger.info("Trigger re-indexing by requesting {}", url);
    ResponseEntity<String> response = this.restTemplate.postForEntity(url, null, String.class);

    if (response.getStatusCode() != HttpStatus.CREATED) {
      logger.warn("Something went wrong while requesting the reset, server responded with status code: {}", response.getStatusCode());
    } else {
      logger.info("Successfully triggered re-indexing on {}", url);
    }
  }
}
