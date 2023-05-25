package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten.GNDEntity;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten.GNDEntityType;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten.INormdatenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;

class PostProcessingHelperTest {

  private final INormdatenService mockedNormdatenService = mock(INormdatenService.class);

  @Test
  void whenGetNormdatumsIsCalledWithValidURI_thenNormdatumsAreReturned() throws Exception {
    final GNDEntity entity = GNDEntity.builder()
        .withGndId("gnd-id")
        .build();
    Mockito.when(mockedNormdatenService.resolve("valid-URI", new ContentInformation(GNDEntityType.PLACE)))
        .thenReturn(new GNDEntity[]{entity});

    List<GNDEntity> entities = PostProcessingHelper.getNormdatumList("valid-URI", GNDEntityType.PLACE, mockedNormdatenService);

    assertThat(entities, hasSize(1));
    assertThat(entities, hasItem(entity));
  }

  @Test
  void whenGetNormdatumsIsCalledWithInValidURI_thenEmptyListIsReturned() throws Exception {
    Mockito.when(mockedNormdatenService.resolve("valid-URI", new ContentInformation(GNDEntityType.PLACE)))
        .thenReturn(new GNDEntity[]{});

    List<GNDEntity> entities = PostProcessingHelper.getNormdatumList("valid-URI", GNDEntityType.PLACE, mockedNormdatenService);

    assertThat(entities, hasSize(0));
  }


  @AfterEach
  void tearDown() {
    Mockito.reset(mockedNormdatenService);
  }
}