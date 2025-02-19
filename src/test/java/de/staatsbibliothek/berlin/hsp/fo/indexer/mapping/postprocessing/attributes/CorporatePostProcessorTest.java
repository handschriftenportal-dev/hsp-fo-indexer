package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.AuthorityFileService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.GNDEntity;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.GNDEntityType;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.GraphQlService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.impl.AuthorityFileServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.impl.InMemoryAuthorityFileRepository;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ContentInformation;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ResultMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

class CorporatePostProcessorTest {

  CorporatePostProcessor cpp;
  AuthorityFileService authorityFileService;

  @BeforeEach
  void setUp() {
    this.cpp = new CorporatePostProcessor();
    this.authorityFileService = mock(AuthorityFileService.class);
  }

  @Test
  void whenProcessingIsCalledWithExistingId_thenResultIsPreferredName() throws Exception {
    GNDEntity gndEntity = GNDEntity.builder()
        .withPreferredName("preferred-name")
        .withName("base-name")
        .withGndId("gnd-id")
        .build();
    when(authorityFileService.resolve("NORM-219c7047-2ffb-47a0-ae80-a0516a36cd54", new ContentInformation(GNDEntityType.CORPORATE))).thenReturn(new GNDEntity[]{gndEntity});

    final List<String> values = cpp.process(null, List.of("NORM-219c7047-2ffb-47a0-ae80-a0516a36cd54"), ResultMapper.PREFERRED_NAME, authorityFileService);

    assertThat(values, is(notNullValue()));
    assertThat(values, hasSize(1));
    assertThat(values, containsInAnyOrder("preferred-name"));
  }

  @Test
  void whenProcessingIsCalledWithNotExistingId_thenResultIsEmpty() throws Exception {
    when(authorityFileService.resolve("not-existing-id", new ContentInformation(GNDEntityType.CORPORATE))).thenReturn(new GNDEntity[]{});

    final List<String> values = cpp.process(null, List.of("not-existing-id"), ResultMapper.DEFAULT, authorityFileService);

    assertThat(values, is(notNullValue()));
    assertThat(values, hasSize(0));
  }

  @Test
  void whenProcessingIsCalledWithEmptyId_thenProcessIsNotCalled() throws Exception {
    final GraphQlService graphQlService = new GraphQlService(WebClient.builder(), "localhost", "/rest/graphql", 56789, "http");
    authorityFileService = Mockito.spy(new AuthorityFileServiceImpl(graphQlService, new InMemoryAuthorityFileRepository()));
    doReturn(new GNDEntity[]{}).when(authorityFileService).resolve("", new ContentInformation(GNDEntityType.CORPORATE));

    cpp.process(null, List.of(""), ResultMapper.DEFAULT, authorityFileService);

    verify(authorityFileService, never()).findByIdOrName("", GNDEntityType.PLACE.getType());

  }

  @AfterEach
  void tearDown() {
    Mockito.reset(authorityFileService);
  }
}