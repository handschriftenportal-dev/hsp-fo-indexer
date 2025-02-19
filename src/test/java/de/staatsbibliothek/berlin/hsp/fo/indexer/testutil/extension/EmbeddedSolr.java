package de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.extension;

import com.google.common.io.Files;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.EmbeddedSolrServer;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.EmbeddedSolrServer.ClientCache;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.annotation.EmbeddedSolrInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

/**
 * Test extension that provides an instance of {@link
 * org.apache.solr.client.solrj.embedded.EmbeddedSolrServer} and injects this instance to any field
 * annotated with {@link EmbeddedSolrInstance} of the test instance
 */
@Slf4j
public class EmbeddedSolr {
  private final ClassPathResource confDir;

  private final EmbeddedSolrServer server;

  private File tempFolder;

  public EmbeddedSolr(final ClassPathResource confDir) {
    this.confDir = confDir;
    this.server = EmbeddedSolrServer.configure(confDir, ClientCache.ENABLED);
  }

  public void prepare() throws IOException {
    tempFolder = Files.createTempDir();

    if (confDir != null && confDir.exists() && confDir.getFile()
        .isDirectory()) {
      FileUtils.copyDirectory(confDir.getFile(), tempFolder);
      log.debug("Copied {} files from {} to temp folder.", confDir.getFile()
          .list().length, confDir.getFile()
          .getPath());
    }
    server.init(tempFolder.getPath());
  }

  public void cleanUp() {
    log.debug("Shutting down CoreContainer");
    server.stopCoreContainer();

    log.debug("Removing temp folder {}", tempFolder.getPath());
    tempFolder.delete();
  }

  public EmbeddedSolrServer getSolrServer() {
    return server;
  }
}
