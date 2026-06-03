package de.staatsbibliothek.berlin.hsp.fo.indexer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
public class GraphQlClientConfig {

  @Bean
  HttpGraphQlClient httpGraphQlClient(
      final WebClient.Builder webClientBuilder,
      @Value("${authority-file.id}") String serviceName,
      @Value("${authority-file.path}") String path,
      @Value("${authority-file.protocol}") String protocol) {

    String baseUri = UriComponentsBuilder.newInstance()
        .scheme(protocol)
        .host(serviceName)
        .path(path)
        .build()
        .toUriString();

    WebClient webClient = webClientBuilder
        .baseUrl(baseUri)
        .build();

    return HttpGraphQlClient.builder(webClient).build();
  }
}
