package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.nachweis;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.DependantServiceException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


/**
 * Allows requesting the NachweisService via HTTP
 */

@Component
@Slf4j
public class NachweisHttpAdapter {

  private final String nachweisServiceId;

  private final RestTemplate restTemplate;
  private String nachweisUrl;

  @Autowired
  public NachweisHttpAdapter(
      @Value("${nachweis.id}") final String nachweisServiceId,
      @Autowired RestTemplate restTemplate) {
    this.nachweisServiceId = nachweisServiceId;
    this.restTemplate = restTemplate;
  }

  @PostConstruct
  private void postConstruct() {
    this.nachweisUrl = String.format("http://%s/rest/index", this.nachweisServiceId);
  }

  public void triggerIndexing(final DocumentType documentType) throws DependantServiceException {
    final String uri = buildURI(this.nachweisUrl, documentType);
    log.info("Trigger re-indexing by requesting {}", uri);
    try {
      ResponseEntity<String> response = this.restTemplate.postForEntity(uri, null, String.class);
      if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
        log.warn("Something went wrong while requesting the reset, server responded with status code: {}", response.getStatusCode());
      } else {
        log.info("Successfully triggered re-indexing on {}", uri);
      }
    } catch (IllegalArgumentException e) {
      log.error("Nachweis service is not available, will stop and retry");
      throw new DependantServiceException("Nachweis service is not available, will stop and retry", e, true, true);
    }
  }

  private static String buildURI(final String url, final DocumentType documentType) {
    return UriComponentsBuilder
        .fromHttpUrl(url)
        .queryParam("docType", documentType)
        .build()
        .toUriString();
  }
}
