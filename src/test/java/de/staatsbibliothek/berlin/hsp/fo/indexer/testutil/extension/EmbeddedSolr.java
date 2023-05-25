package de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.extension;

import com.google.common.io.Files;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.EmbeddedSolrServer;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.EmbeddedSolrServer.ClientCache;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.annotation.EmbeddedSolrInstance;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

/**
 * Test extension that provides an instance of {@link
 * org.apache.solr.client.solrj.embedded.EmbeddedSolrServer} and injects this instance to any field
 * annotated with {@link EmbeddedSolrInstance} of the test instance
 */
public class EmbeddedSolr {

  private static final Logger logger = LoggerFactory.getLogger(EmbeddedSolr.class);

  private final ClassPathResource confDir;

  private final EmbeddedSolrServer server;

  private File tempFolder;

  public EmbeddedSolr(final ClassPathResource confDir) {
    this.confDir = confDir;
    this.server = EmbeddedSolrServer.configure(confDir, ClientCache.ENABLED);
  }

  public void prepare() throws IOException, SolrServerException, InterruptedException {
    tempFolder = Files.createTempDir();

    if (confDir != null && confDir.exists() && confDir.getFile()
        .isDirectory()) {
      FileUtils.copyDirectory(confDir.getFile(), tempFolder);
      logger.debug("Copied {} files from {} to temp folder.", confDir.getFile()
          .list().length, confDir.getFile()
          .getPath());
    }
    server.init(tempFolder.getPath());
  }

  public void cleanUp() {
    logger.debug("Shutting down CoreContainer");
    server.stopCoreContainer();

    logger.debug("Removing temp folder {}", tempFolder.getPath());
    tempFolder.delete();
  }

  public EmbeddedSolrServer getSolrServer() {
    return server;
  }
}
