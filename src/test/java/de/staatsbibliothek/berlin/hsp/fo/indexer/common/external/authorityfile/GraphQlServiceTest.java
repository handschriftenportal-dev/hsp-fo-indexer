package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.graphql.client.GraphQlClient.RequestSpec;
import org.springframework.graphql.client.GraphQlClient.RetrieveSpec;
import org.springframework.graphql.client.GraphQlTransportException;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = GraphQlServiceTest.RetryTestConfiguration.class)
@TestPropertySource(properties = {
    "retry.max-attempts=3",
    "retry.delay=100",
    "retry.multiplier=1"
})
class GraphQlServiceTest {

  @Autowired
  private GraphQlService graphQlService;

  @Autowired
  private HttpGraphQlClient mockHttpGraphQlClient;

  @BeforeEach
  void setUp() {
    reset(mockHttpGraphQlClient);

    RequestSpec mockRequestSpec = mock(RequestSpec.class);
    RetrieveSpec mockRetrieveSpec = mock(RetrieveSpec.class);

    when(mockHttpGraphQlClient.document(anyString())).thenReturn(mockRequestSpec);
    when(mockRequestSpec.variables(any(Map.class))).thenReturn(mockRequestSpec);
    when(mockRequestSpec.operationName(anyString())).thenReturn(mockRequestSpec);
    when(mockRequestSpec.retrieve(anyString())).thenReturn(mockRetrieveSpec);

    GraphQlTransportException transportException = new GraphQlTransportException(
        "Connection refused", new RuntimeException("Connection refused"), mock(GraphQlRequest.class)
    );

    when(mockRetrieveSpec.toEntity(any(Class.class))).thenReturn(Mono.error(transportException));
  }

  @Test
  void whenTransportExceptionIsThrown_thenFindIsRetried() {
    assertThrows(GraphQlTransportException.class,
        () -> graphQlService.find("query {}", Map.of(), "op", "path", String.class));

    verify(mockHttpGraphQlClient, times(3)).document(anyString());
  }

  @Test
  void whenRetriesExhausted_thenExceptionPropagates() {
    GraphQlTransportException thrown = assertThrows(GraphQlTransportException.class,
        () -> graphQlService.find("query {}", Map.of(), "op", "path", String.class));

    assertTrue(thrown.getMessage().contains("Connection refused"));
  }

  @Configuration
  @EnableRetry
  static class RetryTestConfiguration {

    @Bean
    HttpGraphQlClient mockHttpGraphQlClient() {
      return mock(HttpGraphQlClient.class);
    }

    @Bean
    GraphQlService graphQlService(HttpGraphQlClient mockHttpGraphQlClient) {
      return new GraphQlService(mockHttpGraphQlClient);
    }
  }
}
