package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten.GNDEntity;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten.GNDEntityType;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten.INormdatenService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten.NormdatenService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ContentInformation;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ResultMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

class CorporatePostProcessorTest {

  CorporatePostProcessor cpp;
  INormdatenService normdatenService;

  @BeforeEach
  void setUp() {
    this.cpp = new CorporatePostProcessor();
    this.normdatenService = mock(INormdatenService.class);
  }

  @Test
  void whenProcessingIsCalledWithExistingId_thenResultIsPreferredName() throws Exception {
    GNDEntity gndEntity = GNDEntity.builder()
        .withPreferredName("preferred-name")
        .withName("base-name")
        .withGndId("gnd-id")
        .build();
    when(normdatenService.resolve("existing-id", new ContentInformation(GNDEntityType.CORPORATE))).thenReturn(new GNDEntity[]{gndEntity});

    final List<String> values = cpp.process(null, List.of("existing-id"), ResultMapper.PREFERRED_NAME, normdatenService);

    assertThat(values, is(notNullValue()));
    assertThat(values, hasSize(1));
    assertThat(values, containsInAnyOrder("preferred-name"));
  }

  @Test
  void whenProcessingIsCalledWithNotExistingId_thenResultIsEmpty() throws Exception {
    when(normdatenService.resolve("not-existing-id", new ContentInformation(GNDEntityType.CORPORATE))).thenReturn(new GNDEntity[]{});

    final List<String> values = cpp.process(null, List.of("not-existing-id"), ResultMapper.DEFAULT, normdatenService);

    assertThat(values, is(notNullValue()));
    assertThat(values, hasSize(0));
  }

  @Test
  void whenProcessingIsCalledWithEmptyId_thenProcessIsNotCalled() throws Exception {
    normdatenService = Mockito.spy(new NormdatenService("testHost"));
    doReturn(new GNDEntity[]{}).when(normdatenService).resolve("", new ContentInformation(GNDEntityType.CORPORATE));

    cpp.process(null, List.of(""), ResultMapper.DEFAULT, normdatenService);

    verify(normdatenService, never()).findByIdOrName("", GNDEntityType.PLACE.getType());

  }

  @AfterEach
  void tearDown() {
    Mockito.reset(normdatenService);
  }
}