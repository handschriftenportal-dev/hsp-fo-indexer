package de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.extension;

import org.junit.jupiter.api.extension.*;

public class MocoExtensionMethodLevel extends AbstractMocoExtension implements BeforeEachCallback, AfterEachCallback {

  /**
   * @param extensionContext the extension's context
   */
  @Override
  public void afterEach(ExtensionContext extensionContext) {
    stopMoco();
  }

  /**
   * @param extensionContext the extension's context
   * @throws Exception
   */
  @Override
  public void beforeEach(ExtensionContext extensionContext) {
    startMoco(extensionContext);
  }
}
