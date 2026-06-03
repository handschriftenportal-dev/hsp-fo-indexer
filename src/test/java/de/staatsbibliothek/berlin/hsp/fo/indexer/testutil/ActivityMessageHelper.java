package de.staatsbibliothek.berlin.hsp.fo.indexer.testutil;

import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.model.ActivityStreamMessage;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.model.ActivityStreamMessage.ActivityStreamMessageBuilder;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.model.HSPActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.tei_c.ns._1.TEI;

import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ActivityMessageHelper {

  /**
   * Builds an {@link ActivityStreamMessage} based on the given {@link Resource}
   * @param resource representation of the message's content
   * @param type {@link ActivityStreamsDokumentTyp}
   * @return the message
   * @throws IOException
   * @throws JAXBException
   * @throws ActivityStreamsException
   */
  public static ActivityStreamMessage fromResource(final Resource resource, final ActivityStreamsDokumentTyp type) throws IOException, JAXBException, ActivityStreamsException {
    final TEI content = TEIObjectFactory.unmarshal(resource.getInputStream()).getFirst();
    final ActivityStreamObject obj = buildHSPActivityStreamObject(content, type);
    return buildActivityStreamMessage(obj);
  }

  /**
   * builds an {@link ActivityStreamMessage} containing the given <code>kod</code> and <code>descriptionRes</code>>
   *
   * @param hspObjectRes   resource containing XML of an HspObject
   * @param hspDescriptionRes resource(s) containing XML of on or many HspDescriptions
   * @return the built messages
   * @throws Exception
   */
  public static ActivityStreamMessage fromResource(final Resource hspObjectRes, final Resource... hspDescriptionRes) throws Exception {
    Assert.notNull(hspObjectRes, "you need to pass a kod resource!");

    TEIWithType[] teiWithTypes = new TEIWithType[hspDescriptionRes.length + 1];
    teiWithTypes[0] = new TEIWithType(ActivityStreamsDokumentTyp.KOD, hspObjectRes);

    for (int i = 0; i < hspDescriptionRes.length; i++) {
      teiWithTypes[i + 1] = new TEIWithType(ActivityStreamsDokumentTyp.BESCHREIBUNG, hspDescriptionRes[i]);
    }
    return ActivityMessageHelper.getActivityStreamMessageFromXML(teiWithTypes);
  }

  private static ActivityStreamMessage buildActivityStreamMessage(final ActivityStreamObject activityObject) throws ActivityStreamsException {
    return (ActivityStreamMessage) ActivityStreamMessage.builder()
        .withType(ActivityStreamAction.ADD)
        .addObject(activityObject)
        .build();
  }

  private static HSPActivityStreamObject buildHSPActivityStreamObject(final TEI content, final ActivityStreamsDokumentTyp type) throws ActivityStreamsException {
    return (HSPActivityStreamObject) HSPActivityStreamObject.builder()
        .withType(type)
        .withContent(content)
        .build();
  }

  private static List<TEI> buildTEI(final InputStream xmlInputStream) throws JAXBException {
    return TEIObjectFactory.unmarshal(xmlInputStream);
  }

  private static ActivityStreamMessage getActivityStreamMessageFromXML(TEIWithType... resources) throws Exception {
    return getActivityStreamMessageFromXML(ActivityStreamAction.ADD, List.of(resources));
  }

  private static ActivityStreamMessage getActivityStreamMessageFromXML(final ActivityStreamAction action, final List<TEIWithType> res) throws Exception {
    Assert.notNull(res, "res must not be null");
    ActivityStreamMessageBuilder builder = (ActivityStreamMessageBuilder) ActivityStreamMessage.builder().withType(action);

    for (TEIWithType twt : res) {
      final List<TEI> tei = buildTEI(twt.getRes().getInputStream());
      if (!tei.isEmpty()) {
        builder.addObject(buildHSPActivityStreamObject(tei.getFirst(), twt.getType()));
      }
    }
    return (ActivityStreamMessage) builder.build();
  }

  @AllArgsConstructor
  @Data
  public static class TEIWithType {
    private ActivityStreamsDokumentTyp type;

    private Resource res;
  }
}
