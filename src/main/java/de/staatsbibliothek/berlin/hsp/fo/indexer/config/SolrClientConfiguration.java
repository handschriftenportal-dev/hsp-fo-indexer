package de.staatsbibliothek.berlin.hsp.fo.indexer.config;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.SolrHttpRequestRetryHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Objects;

@Configuration
public class SolrClientConfiguration {

  @Bean("leaderClient")
  @Primary
  public SolrClient getLeaderClient(@Value("${solr.leader}") final String solrHost,
                                    @Value("${solr.timeout.connection:5000}") final int connectionTimeout,
                                    @Value("${solr.timeout.read:60000}") final int readTimeout) {
    return getSolrClient(solrHost, connectionTimeout, readTimeout);
  }

  @Bean("followerClient")
  public SolrClient getFollowerClient(@Value("${solr.follower:#{null}}") final String solrHost,
                                      @Value("${solr.timeout.connection:5000}") final int connectionTimeout,
                                      @Value("${solr.timeout.read:60000}") final int readTimeout) {
    if(Objects.nonNull(solrHost)) {
      return getSolrClient(solrHost, connectionTimeout, readTimeout);
    }
    return null;
  }

  private SolrClient getSolrClient(final String solrHost, final int connectionTimeout, final int readTimeout) {
    final String solrUrl = buildUrl(solrHost);
    return new HttpSolrClient.Builder()
        .withBaseSolrUrl(solrUrl)
        .withHttpClient(HttpClientBuilder
            .create()
            .setRetryHandler(new SolrHttpRequestRetryHandler())
            .build())
        .withConnectionTimeout(connectionTimeout)
        .withSocketTimeout(readTimeout)
        .build();
  }

  private String buildUrl(final String host) {
    return String.format("%s/solr", host);
  }
}
