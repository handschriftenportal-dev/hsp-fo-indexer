package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten;

import com.jayway.jsonpath.PathNotFoundException;
import com.netflix.graphql.dgs.client.*;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ContentInformation;
import de.staatsbibliothek.berlin.hsp.fo.indexer.api.IndexerHealthIndicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Service for fetching Normdaten from a given graphQL endpoint
 */
@Service
public class NormdatenService implements INormdatenService {

  private static final Logger logger = LoggerFactory.getLogger(NormdatenService.class);

  //@formatter:off
  private static final String QUERY = ("query gndentity($id: String, $label: String) {"
            + "  findGNDEntityFacts(idOrName: $id, nodeLabel: $label) {"
            + "    gndId: gndIdentifier"
            + "    id"
            + "    identifier {"
            + "      text"
            + "      type"
            + "      url"
            + "    }"
            + "    preferredName"
            + "    variantName {"
            + "      name"
            + "      languageCode"
            + "    }"
            + "  }"
            + "}").replaceAll("\\p{javaSpaceChar}{2,}", " ");
  //@formatter:on
  private final CustomGraphQLClient client;
  private final Class<?> clazz;
  private final Map<String, GNDEntity[]> cache = new HashMap<>();
  private RestTemplate restTemplate;
  final RequestExecutor requestExecutor = (url, headers, body) -> {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.putAll(headers);
    try {
      ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, httpHeaders), String.class);
      return new HttpResponse(exchange.getStatusCodeValue(), exchange.getBody());
    } catch (RestClientException e) {
      IndexerHealthIndicator.setUnhealthyException(new NormdatumException(e.getMessage(), e, true, true));
      logger.warn("error while fetching normdatum {}:{}", e.getMessage(), e);
      return null;
    }
  };

  public NormdatenService(
      @Value("${normdaten.id}") final String normdatenServiceHost) {
    final String url = String.format("http://%s/rest/graphql", normdatenServiceHost);
    this.client = new CustomGraphQLClient(url, requestExecutor);
    this.clazz = GNDEntity[].class;
  }

  @Autowired
  public void setRestTemplate(final RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public GNDEntity[] findByIdOrName(final String idOrName, final String nodeLabel) throws NormdatumException {
    if (cache.containsKey(idOrName)) {
      return cache.get(idOrName);
    }
    try {
      final GraphQLResponse response = client.executeQuery(QUERY, Map.of("id", idOrName, "label", nodeLabel), "gndentity");
      return handleResponse(response, idOrName, nodeLabel);
    } catch (HttpClientErrorException e) {
      logger.info("Invalid request. Probably of a wrong id or name ({}). {}:{}", idOrName, e.getMessage(), e);
    } catch (PathNotFoundException e) {
      logger.info("Invalid response object. Probably server returned 'null' because of a wrong id. {}:{}", e.getMessage(), e);
    } catch (IllegalArgumentException e) {
      if ("Service Instance cannot be null".equals(e.getMessage())) {
        throw new NormdatumException(e.getMessage(), e, true, true);
      } else {
        throw e;
      }
    }
    return new GNDEntity[]{};
  }

  @Override
  public void resetCache() {
    cache.clear();
  }

  @Override
  public void resetCache(String id) {
    cache.remove(id);
  }

  @Override
  public Object resolve(String uri, ContentInformation contentInformation) throws NormdatumException {
    return findByIdOrName(uri, contentInformation.getType()
        .getType());
  }

  private GNDEntity[] handleResponse(final GraphQLResponse response, final String idOrName, final String nodeLabel) throws NormdatumException {
    if (response.hasErrors()) {
      handleErrors(response, idOrName, nodeLabel);
      return new GNDEntity[]{};
    } else {
      return handleData(response, idOrName);
    }
  }

  private void handleErrors(final GraphQLResponse response, final String idOrName, final String nodeLabel) throws NormdatumException {
    for (GraphQLError error : response.getErrors()) {
      if (Objects.equals(ErrorType.UNAVAILABLE, Objects.requireNonNull(error.getExtensions())
          .getErrorType())) {
        throw new NormdatumException(error.getMessage(), true, true);
      } else {
        logger.warn("An error occurred while querying NormdatenService for {} with node {}: {}", idOrName, nodeLabel, error.getMessage());
      }
    }
  }

  private GNDEntity[] handleData(final GraphQLResponse response, final String idOrName) {
    final GNDEntity[] hspNormdatum = (GNDEntity[]) response.extractValueAsObject("findGNDEntityFacts", clazz);
    cache.put(idOrName, hspNormdatum);
    return hspNormdatum;
  }
}
