package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile;

import com.github.dreamhead.moco.HttpServer;
import com.github.dreamhead.moco.Runner;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.impl.InMemoryAuthorityFileRepository;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.impl.AuthorityFileServiceImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;

import static com.github.dreamhead.moco.Moco.pathResource;
import static com.github.dreamhead.moco.MocoJsonRunner.jsonHttpServer;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;

/**
 * test class containing AuthorityFileServiceImpl tests
 */

class AuthorityFileServiceTest {

  static AuthorityFileService authorityFileService;
  static Runner runner;

  @BeforeAll
  public static void beforeAll() {
    final GraphQlService graphQlService = new GraphQlService(WebClient.builder(), "localhost", "/rest/graphql", 56789, "http");
    authorityFileService = new AuthorityFileServiceImpl(graphQlService, new InMemoryAuthorityFileRepository());
    HttpServer server = jsonHttpServer(56789, pathResource("moco/authorityfiles.json", StandardCharsets.UTF_8));
    runner = Runner.runner(server);
    runner.start();
  }

  @AfterAll
  public static void afterAll() {
    runner.stop();
  }

  @Test
  void whenCalledWithValidId_ThenCorrectAuthorityFileIsReturned() throws Exception {
    GNDEntity[] gndEntity = authorityFileService.findByIdOrName("NORM-909a19c7-84f8-4151-8702-5cd384b2dd7e", GNDEntityType.CORPORATE.getType());
    assertThat(gndEntity, arrayWithSize(1));
    assertThat(gndEntity[0].getGndId(), is("gnd test id"));
    assertThat(gndEntity[0].getId(), is("NORM-909a19c7-84f8-4151-8702-5cd384b2dd7e"));
    assertThat(gndEntity[0].getName(), is("test name"));
    assertThat(gndEntity[0].getPreferredName(), is("test preferred name"));
    assertThat(gndEntity[0].getVariantName(), arrayWithSize(2));
    assertThat(gndEntity[0].getVariantName()[0], is(new GNDEntity.Variant("test variant name 01", "de")));
    assertThat(gndEntity[0].getVariantName()[1], is(new GNDEntity.Variant("test variant name 02", "de")));
    assertThat(gndEntity[0].getIdentifier()[0].getText(), is("test text"));
    assertThat(gndEntity[0].getIdentifier()[0].getType(), is("test type"));
    assertThat(gndEntity[0].getIdentifier()[0].getUrl(), is("test url"));
  }

  @Test
  void whenCalledWithInvalidId_ThenNullIsReturned() throws Exception {
    GNDEntity[] gndEntity = authorityFileService.findByIdOrName("wrong test id", "");
    assertThat(gndEntity, is(nullValue()));
  }
}
