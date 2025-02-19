package de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.fixture;

import com.github.dreamhead.moco.HttpServer;
import com.github.dreamhead.moco.Runnable;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.AuthorityFileService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.GraphQlService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.impl.InMemoryAuthorityFileRepository;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.impl.AuthorityFileServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.SolrMapper;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.dreamhead.moco.Moco.pathResource;
import static com.github.dreamhead.moco.MocoJsonRunner.jsonHttpServer;
import static com.github.dreamhead.moco.Runner.running;

public class AuthorityFileFixture {

  private static AuthorityFileService authorityFileService;

  public static AuthorityFileService getAuthorityFileService() {
    if(authorityFileService == null) {
      final GraphQlService graphQlService = new GraphQlService(WebClient.builder(), "localhost", "/rest/graphql", 56789, "http");
      authorityFileService = new AuthorityFileServiceImpl(graphQlService, new InMemoryAuthorityFileRepository());
    }
    return authorityFileService;
  }

  public static void runWithAuthorityFileService(final Runnable runnable, final SolrMapper solrMapper) throws Exception {
    runWithAuthorityFileService(runnable, solrMapper, "moco/authorityfiles.json");
  }

  public static void runWithAuthorityFileService(final Runnable runnable, final SolrMapper solrMapper, final String jsonApiFile) throws Exception {
    final HttpServer server = jsonHttpServer(56789, pathResource(jsonApiFile));
    final GraphQlService graphQlService = new GraphQlService(WebClient.builder(), "localhost", "/rest/graphql", 56789, "http");
    final AuthorityFileService authorityFileService = getAuthorityFileService();
    solrMapper.setAuthorityFileService(authorityFileService);
    running(server, runnable);
  }
}
