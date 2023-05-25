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

import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ActivityMessageHelper {

  public static ActivityStreamMessage buildActivityStreamMessage(final String id, final ActivityStreamObject activityObject) throws ActivityStreamsException {
    return (ActivityStreamMessage) ActivityStreamMessage.builder()
        .withType(ActivityStreamAction.ADD)
        .withId(id)
        .addObject(activityObject)
        .build();
  }

  public static HSPActivityStreamObject buildHSPActivityStreamObject(final ActivityStreamsDokumentTyp type, final TEI content) throws ActivityStreamsException {
    return (HSPActivityStreamObject) HSPActivityStreamObject.builder()
        .withType(type)
        .withContent(content)
        .build();
  }

  public static TEIWithType getASDKOD(final Resource content) {
    return new TEIWithType(ActivityStreamsDokumentTyp.KOD, content);
  }

  public static TEIWithType getASDBeschreibung(final Resource content) {
    return new TEIWithType(ActivityStreamsDokumentTyp.BESCHREIBUNG, content);
  }

  public static List<TEI> buildTEI(final InputStream xmlInputStream) throws JAXBException {
    return TEIObjectFactory.unmarshal(xmlInputStream);
  }

  public static ActivityStreamMessage getActivityStreamMessageFromXML(final List<TEIWithType> res) throws Exception {
    return getActivityStreamMessageFromXML(ActivityStreamAction.ADD, res);
  }

  public static ActivityStreamMessage getActivityStreamMessageFromXML(TEIWithType... resources) throws Exception {
    return getActivityStreamMessageFromXML(ActivityStreamAction.ADD, List.of(resources));
  }

  /**
   * builds an {@link ActivityStreamMessage} containing the given <code>kod</code> and <code>descs</code>>
   *
   * @param kod   resource containing XML of an KulturObjektDokument
   * @param descs resource(s) containing XML of on or many Beschreibungen
   * @return the built messages
   * @throws Exception
   */
  public static ActivityStreamMessage getActivityStreamMessageFromResources(final Resource kod, final Resource... descs) throws Exception {
    Assert.notNull(kod, "you need to pass a kod resource!");
    List<TEIWithType> twts = new ArrayList<>();
    twts.add(new TEIWithType(ActivityStreamsDokumentTyp.KOD, kod));
    for (Resource descRes : descs) {
      twts.add(new TEIWithType(ActivityStreamsDokumentTyp.BESCHREIBUNG, descRes));
    }
    return ActivityMessageHelper.getActivityStreamMessageFromXML(twts);
  }

  public static ActivityStreamMessage getActivityStreamMessageFromXML(final ActivityStreamAction action, final List<TEIWithType> res) throws Exception {
    Assert.notNull(res, "res must not be null");

    ActivityStreamMessageBuilder builder = (ActivityStreamMessageBuilder) ActivityStreamMessage.builder()
        .withType(action);

    for (TEIWithType twt : res) {
      final List<TEI> tei = buildTEI(twt.getRes()
          .getInputStream());
      if (tei != null && tei.size() > 0) {
        builder.addObject(buildHSPActivityStreamObject(twt.getType(), tei.get(0)));
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
