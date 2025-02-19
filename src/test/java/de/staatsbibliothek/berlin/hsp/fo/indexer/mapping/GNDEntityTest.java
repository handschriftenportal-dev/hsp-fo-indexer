package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.GNDEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * test class for GNDEntity tests
 */

class GNDEntityTest {

  /* see https://projects.dev.sbb.berlin/issues/12634 */
  @Test
  void giveEmptyGNDEntity_whenGetAsListISCalled_thenNoExceptionIsThrown() {
    GNDEntity gndEntity = new GNDEntity();
    assertDoesNotThrow(() -> {
      gndEntity.getAsList();
    });
  }
}
