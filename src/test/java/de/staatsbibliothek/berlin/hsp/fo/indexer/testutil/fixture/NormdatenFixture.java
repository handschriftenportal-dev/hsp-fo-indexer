package de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.fixture;

import com.github.dreamhead.moco.HttpServer;
import com.github.dreamhead.moco.Runnable;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten.NormdatenService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.SolrMapper;
import org.springframework.web.client.RestTemplate;

import static com.github.dreamhead.moco.Moco.pathResource;
import static com.github.dreamhead.moco.MocoJsonRunner.jsonHttpServer;
import static com.github.dreamhead.moco.Runner.running;

public class NormdatenFixture {

  public static void runWithNormdatenService(final Runnable runnable, final SolrMapper solrMapper) throws Exception {
    runWithNormdatenService(runnable, solrMapper, "moco/normdatums.json");
  }

  public static void runWithNormdatenService(final Runnable runnable, final SolrMapper solrMapper, final String jsonApiFile) throws Exception {
    final HttpServer server = jsonHttpServer(56789, pathResource(jsonApiFile));
    NormdatenService normService = new NormdatenService("localhost:56789");
    normService.setRestTemplate(new RestTemplate());
    solrMapper.setNormDatenService(normService);
    running(server, runnable);
  }
}
