package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normaten;

import com.github.dreamhead.moco.HttpServer;
import com.github.dreamhead.moco.Runner;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten.GNDEntity;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten.GNDEntityType;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten.NormdatenService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

import static com.github.dreamhead.moco.Moco.pathResource;
import static com.github.dreamhead.moco.MocoJsonRunner.jsonHttpServer;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;

/**
 * test class containing NormdatenService tests
 */

class NormdatenServiceTests {

  static NormdatenService normService;
  static Runner runner;

  @BeforeAll
  public static void beforeAll() throws Exception {
    normService = new NormdatenService("localhost:56789");
    normService.setRestTemplate(new RestTemplate());
    HttpServer server = jsonHttpServer(56789, pathResource("moco/normdatums.json", StandardCharsets.UTF_8));
    runner = Runner.runner(server);
    runner.start();
  }

  @AfterAll
  public static void afterAll() {
    runner.stop();
  }

  @Test
  void whenCalledWithValidId_ThenCorrectNormdatumIsReturned() throws Exception {
    GNDEntity[] normDatum = normService.findByIdOrName("test id", GNDEntityType.CORPORATE.getType());
    assertThat(normDatum, arrayWithSize(1));
    assertThat(normDatum[0].getGndId(), is("gnd test id"));
    assertThat(normDatum[0].getId(), is("test id"));
    assertThat(normDatum[0].getName(), is("test name"));
    assertThat(normDatum[0].getPreferredName(), is("test preferred name"));
    assertThat(normDatum[0].getVariantName(), arrayWithSize(2));
    assertThat(normDatum[0].getVariantName()[0], is(new GNDEntity.Variant("test variant name 01", "de")));
    assertThat(normDatum[0].getVariantName()[1], is(new GNDEntity.Variant("test variant name 02", "de")));
    assertThat(normDatum[0].getIdentifier()[0].getText(), is("test text"));
    assertThat(normDatum[0].getIdentifier()[0].getType(), is("test type"));
    assertThat(normDatum[0].getIdentifier()[0].getUrl(), is("test url"));
  }

  @Test
  void whenCalledWithInvalidId_ThenNullIsReturned() throws Exception {
    GNDEntity[] normDatum = normService.findByIdOrName("wrong test id", "");
    assertThat(normDatum, is(nullValue()));
  }
}
