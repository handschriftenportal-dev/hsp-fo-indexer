package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class GraphQlService {
  private final HttpGraphQlClient httpGraphQlClient;

  public GraphQlService(final WebClient.Builder webClientBuilder,
                              @Value("${authority-file.id}") String serviceName,
                              @Value("${authority-file.path}") String path,
                              @Value("${authority-filen.port:#{null}}") Integer port,
                              @Value("${authority-file.protocol}") String protocol) {
    this.httpGraphQlClient = configure(webClientBuilder, serviceName, path, port, protocol);
  }

  private HttpGraphQlClient configure(final WebClient.Builder webClientBuilder, final String serviceName, final String path, final Integer port, final String protocol) {
    UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
        .scheme(protocol)
        .host(serviceName)
        .path(path);

    if(port != null) {
      builder.port(port);
    }
    final String baseUri = builder.build().toUriString();
    final CloseableHttpAsyncClient asyncClient = getCloseableHttpAsyncClient();
    asyncClient.start();
    WebClient webClient = webClientBuilder
        .baseUrl(baseUri)
        .clientConnector(new HttpComponentsClientHttpConnector(asyncClient))
        .build();
    return HttpGraphQlClient.builder(webClient).build();
  }

  public <T> T find(final String query, final Map<String, Object> variables, final String operation, final String resultPath, final Class<T> clazz) {
    return httpGraphQlClient
        .document(query)
        .variables(variables)
        .operationName(operation)
        .retrieve(resultPath)
        .toEntity(clazz)
        .block();
  }

  protected CloseableHttpAsyncClient getCloseableHttpAsyncClient() {
    return HttpAsyncClients.custom()
        .setDefaultRequestConfig(RequestConfig.custom()
            .setConnectTimeout(Timeout.ofMinutes(1))
            .setResponseTimeout(Timeout.ofMinutes(1))
            .build())
        .build();
  }
}
