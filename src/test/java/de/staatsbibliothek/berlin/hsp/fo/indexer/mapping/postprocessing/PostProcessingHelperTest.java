package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.AuthorityFileService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.GNDEntity;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.GNDEntityType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;

class PostProcessingHelperTest {

  private final AuthorityFileService mockedAuthorityFileService = mock(AuthorityFileService.class);

  @Test
  void whenGetAuthorityFilesIsCalledWithValidURI_thenAuthorityFilesAreReturned() throws Exception {
    final GNDEntity entity = GNDEntity.builder()
        .withGndId("gnd-id")
        .build();
    Mockito.when(mockedAuthorityFileService.resolve("valid-URI", new ContentInformation(GNDEntityType.PLACE)))
        .thenReturn(new GNDEntity[]{entity});

    List<GNDEntity> entities = PostProcessingHelper.getAuthorityFiles("valid-URI", GNDEntityType.PLACE, mockedAuthorityFileService);

    assertThat(entities, hasSize(1));
    assertThat(entities, hasItem(entity));
  }

  @Test
  void whenGetAuthorityFilesIsCalledWithInValidURI_thenEmptyListIsReturned() throws Exception {
    Mockito.when(mockedAuthorityFileService.resolve("valid-URI", new ContentInformation(GNDEntityType.PLACE)))
        .thenReturn(new GNDEntity[]{});

    List<GNDEntity> entities = PostProcessingHelper.getAuthorityFiles("valid-URI", GNDEntityType.PLACE, mockedAuthorityFileService);

    assertThat(entities, hasSize(0));
  }

  @AfterEach
  void tearDown() {
    Mockito.reset(mockedAuthorityFileService);
  }
}