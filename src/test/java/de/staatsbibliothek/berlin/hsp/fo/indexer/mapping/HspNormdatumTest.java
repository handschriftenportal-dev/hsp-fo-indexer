package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten.GNDEntity;
import org.junit.jupiter.api.Test;

/**
 * test class for HspNormdatum tests
 */

class HspNormdatumTest {

  /* see https://projects.dev.sbb.berlin/issues/12634 */
  @Test
  void whenVariantNameIsNull_NoExceptionIsThrown() {
    GNDEntity normDatum = new GNDEntity();
    normDatum.getAsList();
  }
}
