package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile;

import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class GraphQlService {
  private final HttpGraphQlClient httpGraphQlClient;

  public GraphQlService(final HttpGraphQlClient httpGraphQlClient) {
    this.httpGraphQlClient = httpGraphQlClient;
  }

  @Retryable(
      maxAttemptsExpression = "${retry.max-attempts:3}",
      backoff = @Backoff(
          delayExpression = "${retry.delay:1000}",
          multiplierExpression = "${retry.multiplier:2}"
      )
  )
  public <T> T find(final String query, final Map<String, Object> variables, final String operation, final String resultPath, final Class<T> clazz) {
    log.debug("Querying authority file service for operation: {}", operation);
    return httpGraphQlClient
        .document(query)
        .variables(variables)
        .operationName(operation)
        .retrieve(resultPath)
        .toEntity(clazz)
        .block();
  }
}