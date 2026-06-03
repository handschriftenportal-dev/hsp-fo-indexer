package de.staatsbibliothek.berlin.hsp.fo.indexer.common.util;

import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.model.ActivityStreamMessage;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Optional;

public class ActivityStreamMessageHelper {

  private ActivityStreamMessageHelper() {}

  public static List<ActivityStreamObject> getActivityStreamObjectsByType(final ActivityStreamMessage activityStreamMessage, final ActivityStreamsDokumentTyp type) {
    return CollectionUtils.emptyIfNull(activityStreamMessage.getObjects()).stream()
            .filter(aso -> aso.getType().equals(type))
            .toList();
  }

  public static Optional<ActivityStreamsDokumentTyp> determineType(final ActivityStreamMessage asm) {
    if(!asm.getObjects().isEmpty()) {
      return Optional.of(asm.getObjects().getFirst().getType());
    }
    return Optional.empty();
  }
}
