package de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.extension;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class MocoExtensionClassLevel extends AbstractMocoExtension implements BeforeAllCallback, AfterAllCallback {
  /**
   * @param extensionContext the extension's context
   */
  @Override
  public void beforeAll(final ExtensionContext extensionContext) {
    startMoco(extensionContext);
  }

  /**
   * @param extensionContext the extension's context
   */
  @Override
  public void afterAll(ExtensionContext extensionContext) {
    stopMoco();
  }
}
