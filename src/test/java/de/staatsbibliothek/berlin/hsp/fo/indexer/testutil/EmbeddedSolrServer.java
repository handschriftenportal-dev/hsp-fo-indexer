/*
 * Copyright 2016-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package de.staatsbibliothek.berlin.hsp.fo.indexer.testutil;

import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryRequestBase;
import org.apache.solr.servlet.SolrRequestParsers;
import org.apache.solr.util.RTimerTree;
import org.junit.rules.ExternalResource;
import org.mockito.Mockito;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.when;

/**
 * {@link ExternalResource} wrapping a {@link CoreContainer} allowing easy access to {@link
 * SolrClient} for cores configured via an {@link Resource}. Configuration options will be copied to
 * a temp folder and removed afterward.
 *
 * @author Christoph Strobl
 * @author Juan Manuel de Blas
 */
@Slf4j
public class EmbeddedSolrServer {
  Resource configDir;
  CoreContainer coreContainer;
  ConcurrentHashMap<String, SolrClient> cachedClients = new ConcurrentHashMap<>();
  private ClientCache clientCache = ClientCache.DISABLED;

  private EmbeddedSolrServer() {
  }

  /**
   * Get a configured {@link EmbeddedSolrServer} using the configDir as configuration source.
   *
   * @param configDir must not be {@literal null}.
   * @return
   */
  public static EmbeddedSolrServer configure(Resource configDir) {
    return configure(configDir, ClientCache.DISABLED);
  }

  /**
   * Get a configured {@link EmbeddedSolrServer} using the configDir as configuration source.
   *
   * @param configDir   must not be {@literal null}.
   * @param clientCache
   * @return
   */
  public static EmbeddedSolrServer configure(Resource configDir, ClientCache clientCache) {

    EmbeddedSolrServer ess = new EmbeddedSolrServer();
    ess.configDir = configDir;
    ess.clientCache = clientCache;
    return ess;
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.solr.server.SolrClientFactory#getSolrClient()
   */
  public SolrClient getSolrClient() {
    return getSolrClient("collection1");
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.solr.server.SolrClientFactory#getSolrClient(java.lang.String)
   */
  @SuppressWarnings("serial")
  public SolrClient getSolrClient(String collectionName) {

    if (ClientCache.ENABLED.equals(clientCache) && cachedClients.containsKey(collectionName)) {
      return cachedClients.get(collectionName);
    }

    org.apache.solr.client.solrj.embedded.EmbeddedSolrServer solrServer = new org.apache.solr.client.solrj.embedded.EmbeddedSolrServer(coreContainer, collectionName) {

      public void shutdown() {
        // ignore close at this point. CoreContainer will be shut down on its own.
      }

      @Override
      public void close() {
        shutdown();
      }
    };

    final DirectFieldAccessor dfa = new DirectFieldAccessor(solrServer);
    dfa.setPropertyValue("_parser", new HttpMethodGuessingSolrRequestParsers());

    if (ClientCache.ENABLED.equals(clientCache)) {
      cachedClients.put(collectionName, solrServer);
    }

    return solrServer;
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.solr.server.SolrClientFactory#getCores()
   */
  public List<String> getCores() {
    return new ArrayList<>(coreContainer.getAllCoreNames());
  }

  public void stopCoreContainer() {
    try {
      coreContainer.shutdown();
      coreContainer = null;
      cachedClients.clear();
    } catch (Exception e) {
      log.error("Error shutting down CoreContainer", e);
    }
  }

  public void init(String solrHome) {

    Method createAndLoadMethod = ClassUtils.getStaticMethod(CoreContainer.class, "createAndLoad", String.class, File.class);

    log.debug("Starting CoreContainer {} and loading cores.", solrHome);

    if (createAndLoadMethod != null) {
      coreContainer = (CoreContainer) ReflectionUtils.invokeMethod(createAndLoadMethod, null, solrHome, new File(solrHome + "/solr.xml"));
    } else {

      createAndLoadMethod = ClassUtils.getStaticMethod(CoreContainer.class, "createAndLoad", Path.class, Path.class);

      coreContainer = (CoreContainer) ReflectionUtils.invokeMethod(createAndLoadMethod, null, FileSystems.getDefault()
          .getPath(solrHome), FileSystems.getDefault()
          .getPath(new File(solrHome + "/solr.xml").getPath()));
    }
    log.debug("CoreContainer up and running - Happy searching :)");
  }

  public enum ClientCache {
    ENABLED,
    DISABLED
  }

  /**
   * Workaround for {@link SolrRequestParsers} which does not read POST requests correctly and treats them as get ones.
   * This happens as the context is not set up correctly since the used http request gets {@code nulled} out :( <br />
   * So we check if there's a {@link ContentStream} available and treat all those requests as POST ones.
   *
   * @author Christoph Strobl
   */
  static class HttpMethodGuessingSolrRequestParsers extends SolrRequestParsers {

    HttpMethodGuessingSolrRequestParsers() {
      this(null);
    }

    HttpMethodGuessingSolrRequestParsers(SolrConfig globalConfig) {
      super(globalConfig);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.solr.servlet.SolrRequestParsers#buildRequestFrom(org.apache.solr.core.SolrCore, org.apache.solr.common.params.SolrParams, java.util.Collection)
     */
    @Override
    public SolrQueryRequest buildRequestFrom(SolrCore core, SolrParams params, Collection<ContentStream> streams) throws Exception {

      if (CollectionUtils.isEmpty(streams)) {
        return super.buildRequestFrom(core, params, streams);
      }

      HttpServletRequest mock = Mockito.mock(HttpServletRequest.class);
      when(mock.getMethod()).thenReturn("POST");

      Method buildRequestFromMethod = org.springframework.util.ReflectionUtils.findMethod(this.getClass(), "buildRequestFrom", SolrCore.class, SolrParams.class, Collection.class, RTimerTree.class, HttpServletRequest.class);
      buildRequestFromMethod.setAccessible(true);

      SolrQueryRequestBase sqr = (SolrQueryRequestBase) buildRequestFromMethod.invoke(this, core, params, streams, new RTimerTree(), mock);

      if (sqr.getContext() == null) {
        new DirectFieldAccessor(sqr).setPropertyValue("context", Collections.singletonMap("httpMethod", "POST"));
      } else {
        sqr.getContext()
            .put("httpMethod", "POST");
      }
      return sqr;
    }
  }
}
