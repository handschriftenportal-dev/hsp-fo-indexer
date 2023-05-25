package de.staatsbibliothek.berlin.hsp.fo.indexer.api;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.HSPException;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Indicates whether the status provided by Spring Boot Actuator is Up or Down,
 * based on the existence of on error
 */

@Component
public class IndexerHealthIndicator implements HealthIndicator {

  private static HSPException unhealthyException;

  public static void resetException() {
    unhealthyException = null;
  }

  public static HSPException getUnhealthyException() {
    return unhealthyException;
  }

  public static void setUnhealthyException(final HSPException exception) {
    unhealthyException = exception;
  }

  public static boolean isHealthy() {
    return unhealthyException == null;
  }

  @Override
  public Health health() {
    if (!isHealthy()) {
      return Health.down()
          .build();
    }
    return Health.up()
        .build();
  }
}