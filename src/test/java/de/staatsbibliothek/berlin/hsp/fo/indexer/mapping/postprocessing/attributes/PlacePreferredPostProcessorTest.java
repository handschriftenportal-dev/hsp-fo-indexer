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
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

class PlacePreferredPostProcessorTest {

  PlacePostProcessor ppp;
  INormdatenService normdatenService;

  @BeforeEach
  void setUp() {
    this.ppp = new PlacePostProcessor();
    this.normdatenService = mock(INormdatenService.class);
  }

  @Test
  void whenProcessingIsCalledWithExistingId_thenResultIsPreferredName() throws Exception {
    GNDEntity gndEntity = new GNDEntity();
    gndEntity.setPreferredName("place preferred name");
    when(normdatenService.resolve("existing-id", new ContentInformation(GNDEntityType.PLACE))).thenReturn(new GNDEntity[]{gndEntity});

    final List<String> values = ppp.process(null, List.of("existing-id"), ResultMapper.DEFAULT, normdatenService);

    assertThat(values, is(notNullValue()));
    assertThat(values, hasSize(1));
    assertThat(values.get(0), is(gndEntity.getPreferredName()));
  }

  @Test
  void whenProcessingIsCalledWithNotExistingId_thenResultEmpty() throws Exception {
    when(normdatenService.resolve("not-existing-id", new ContentInformation(GNDEntityType.PLACE))).thenReturn(new GNDEntity[]{});

    final List<String> values = ppp.process(null, List.of("not-existing-id"), ResultMapper.DEFAULT, normdatenService);

    assertThat(values, is(notNullValue()));
    assertThat(values, hasSize(0));
  }

  @Test
  void whenProcessingIsCalledWithEmptyId_thenProcessIsNotCalled() throws Exception {
    normdatenService = Mockito.spy(new NormdatenService("testHost"));
    doReturn(new GNDEntity[]{}).when(normdatenService).resolve("", new ContentInformation(GNDEntityType.CORPORATE));

    ppp.process(null, List.of(""), ResultMapper.DEFAULT, normdatenService);

    verify(normdatenService, never()).findByIdOrName("", GNDEntityType.PLACE.getType());

  }


  @AfterEach
  void tearDown() {
    Mockito.reset(normdatenService);
  }
}