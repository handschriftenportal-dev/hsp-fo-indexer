package de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.extension;

import com.github.dreamhead.moco.HttpServer;
import com.github.dreamhead.moco.Runner;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.PreconditionViolationException;

import java.util.Optional;

import static com.github.dreamhead.moco.Moco.pathResource;
import static com.github.dreamhead.moco.MocoJsonRunner.jsonHttpServer;
import static com.github.dreamhead.moco.Runner.runner;

public class AbstractMocoExtension {

  private Runner runner;

  /**
   * Retrieves the value of the {@link MocoExtensionFileParam} annotation from the method or class.
   * If the annotation is not found on the method, it checks the class and then the enclosing class.
   *
   * @return The value of the {@link MocoExtensionFileParam} annotation, or {@code null} if not found.
   * @throws IllegalArgumentException If the class is {@code null}.
   */
  private static String gatherMocoFile(final ExtensionContext extensionContext) {
    if (extensionContext.getRequiredTestClass() == null) {
      throw new IllegalArgumentException("Missing class context");
    }
    String file = null;
    try {
      file = Optional.ofNullable(extensionContext.getRequiredTestMethod())
          .map(m -> m.getAnnotation(MocoExtensionFileParam.class))
          .map(MocoExtensionFileParam::value)
          .orElse(null);
    } catch(PreconditionViolationException e) {
      // can be ignored as this likely results of using the extension in class context
    }

    if (StringUtils.isEmpty(file)) {
      file = Optional.ofNullable(extensionContext.getRequiredTestClass().getAnnotation(MocoExtensionFileParam.class))
          .map(MocoExtensionFileParam::value)
          .orElse(null);
    }

    if (StringUtils.isEmpty(file)) {
      return gatherMocoFileFromClass(extensionContext.getRequiredTestClass());
    }

    return file;
  }

  private static String gatherMocoFileFromClass(final Class<?> clazz) {
    String file = Optional.ofNullable(clazz.getAnnotation(MocoExtensionFileParam.class))
        .map(MocoExtensionFileParam::value)
        .orElse(null);
    if(StringUtils.isEmpty(file)) {
      file =gatherMocoFileFromClass(clazz.getEnclosingClass());
    }
    return file;
  }

  protected void startMoco(final ExtensionContext extensionContext) {
    String file = gatherMocoFile(extensionContext);
    final HttpServer server = jsonHttpServer(56789, pathResource(file));
    runner = runner(server);
    runner.start();
  }

  protected void stopMoco() {
    runner.stop();
  }
}
