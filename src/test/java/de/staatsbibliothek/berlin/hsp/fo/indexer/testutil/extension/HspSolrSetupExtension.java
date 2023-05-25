package de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.extension;

import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.annotation.EmbeddedSolrInstance;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.platform.commons.util.AnnotationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.lang.reflect.Field;
import java.util.List;

public class HspSolrSetupExtension extends DefaultSolrSetupExtension implements TestInstancePostProcessor {

  private static final Logger logger = LoggerFactory.getLogger(HspSolrSetupExtension.class);

  public HspSolrSetupExtension() {
    this.solrClient = new EmbeddedSolr(new ClassPathResource("solr"));
  }

  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
    injectSolrServer(testInstance);
  }

  private void injectSolrServer(Object testInstance) {
    List<Field> fields = AnnotationUtils.findAnnotatedFields(testInstance.getClass(), EmbeddedSolrInstance.class, f -> true);

    for (Field f : fields) {
      f.setAccessible(true);
      try {
        f.set(testInstance, this.solrClient.getSolrServer());
      } catch (Exception e) {
        logger.warn("Could not inject solrServer instance. Maybe wrong type?");
      }
    }
  }
}