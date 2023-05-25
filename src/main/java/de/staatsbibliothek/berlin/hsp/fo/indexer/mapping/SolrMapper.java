package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten.INormdatenService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.util.JsonHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.util.TypeHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.ProcessingUnit;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.Selector;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.XMLSource;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ContentResolverException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.PostProcessor;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.xml.XPathEvaluator;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.*;
import de.staatsbibliothek.berlin.hsp.fo.indexer.type.HspObjectType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Maps TEI to Solr entities
 */
@Component
@Slf4j
public class SolrMapper {
  private final PostProcessor postProcessor;
  private INormdatenService normService;

  public SolrMapper() {
    postProcessor = new PostProcessor("de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes", getClass().getClassLoader());
  }

  @Autowired
  public void setNormDatenService(final INormdatenService normService) {
    this.normService = normService;
  }

  public static String getMsIdentifierFromSettlementRepository(final HspBaseDocument hspObjectOrDesc) {
    if (StringUtils.isBlank(hspObjectOrDesc.getSettlementDisplay()) || StringUtils.isBlank(hspObjectOrDesc.getRepositoryDisplay())) {
      return null;
    }
    return String.join(", ", hspObjectOrDesc.getSettlementDisplay(), hspObjectOrDesc.getRepositoryDisplay());
  }

  public static String getMsIdentifierFromSettlementRepositoryIdno(final HspBaseDocument hspObjectOrDesc) {
    final String msIdentifierBase = getMsIdentifierFromSettlementRepository(hspObjectOrDesc);
    if (StringUtils.isBlank(msIdentifierBase) || StringUtils.isBlank(hspObjectOrDesc.getIdnoSearch())) {
      return null;
    }
    return String.join(", ", msIdentifierBase, hspObjectOrDesc.getIdnoSearch());
  }

  public static String getMsIdentifierFromSettlementRepositorySortKeyOrIdno(final HspObject hspObjectOrDesc) {
    final String msIdentifierBase = getMsIdentifierFromSettlementRepository(hspObjectOrDesc);
    if (StringUtils.isBlank(msIdentifierBase) || (StringUtils.isBlank(hspObjectOrDesc.getIdnoSearch()) && StringUtils.isBlank(hspObjectOrDesc.getIdnoSortKey()))) {
      return null;
    }
    final String sortKeyOrIdno = StringUtils.isBlank(hspObjectOrDesc.getIdnoSortKey()) ? hspObjectOrDesc.getIdnoSearch() : hspObjectOrDesc.getIdnoSortKey();

    return String.join(", ", msIdentifierBase, sortKeyOrIdno);
  }

  public static HspObjectGroup injectFacetValuesJson(final HspObjectGroup objectGroup) {
    final List<JsonNode> jsonObjects = getJsonNodes(objectGroup);
    final List<JsonNode> jsonObjectsWithFacets = ListUtils.subtract(jsonObjects, getJsonNodesByType(jsonObjects, HspObjectType.DIGITIZATION));
    final List<String> fieldNames = JsonHelper.getFieldNames(jsonObjectsWithFacets);
    for (String fieldName : fieldNames) {
      if (fieldName.endsWith("Facet") && JsonHelper.isArray(fieldName, jsonObjectsWithFacets)) {
        ArrayNode values = JsonHelper.createJsonArray();
        for (JsonNode jn : jsonObjectsWithFacets) {
          if (!(jn.get(fieldName) instanceof NullNode)) {
            values.addAll((ArrayNode) jn.get(fieldName));
          }
        }
        JsonHelper.removeDuplicates(values);
        for (JsonNode jn : jsonObjectsWithFacets) {
          if (jn.get(fieldName) instanceof NullNode) {
            ((ObjectNode) jn).putArray(fieldName);
          } else {
            ((ArrayNode) jn.get(fieldName)).removeAll();
          }
          ((ArrayNode) jn.get(fieldName)).addAll(values);
        }
      }
    }
    return getHspObjectGroup(jsonObjects);
  }

  private static List<JsonNode> getJsonNodes(HspObjectGroup group) {
    final List<JsonNode> jsonObjects = new ArrayList<>();
    JsonHelper.toJsonTree(group.getHspObject()).ifPresent(jsonObjects::add);

    if (group.getHspDescriptions() != null) {
      for (HspDescription desc : group.getHspDescriptions()) {
        JsonHelper.toJsonTree(desc).ifPresent(jsonObjects::add);
      }
    }
    if (group.getHspDigitized() != null) {
      for (HspDigitized digitalCopy : group.getHspDigitized()) {
        JsonHelper.toJsonTree(digitalCopy).ifPresent(jsonObjects::add);
      }
    }
    return jsonObjects;
  }

  private static HspObjectGroup getHspObjectGroup(final List<JsonNode> nodes) {
    final HspObjectGroup group = new HspObjectGroup();
    JsonHelper.fromJsonTree(getJsonNodesByType(nodes, HspObjectType.OBJECT).get(0), HspObject.class).ifPresent(group::setHspObject);
    group.setHspDescriptions(JsonHelper.fromJsonTree(getJsonNodesByType(nodes, HspObjectType.DESCRIPTION), HspDescription.class));
    group.setHspDigitized(JsonHelper.fromJsonTree(getJsonNodesByType(nodes, HspObjectType.DIGITIZATION), HspDigitized.class));
    return group;
  }

  private static List<JsonNode> getJsonNodesByType(final List<JsonNode> nodes, final HspObjectType type) {
    return nodes.stream()
            .filter(node -> type.equalsValue(node.get("typeSearch").textValue()))
            .collect(Collectors.toList());
  }

  public HspObjectGroup mapHspObjectGroup(@Nullable final byte[] kodContent, List<byte[]> descriptionContents) throws ContentResolverException {
    if(Objects.isNull(kodContent)) {
      return null;
    }

    HspObjectGroup hspObjectGroup = new HspObjectGroup();
    final HspObject hspObject;
    List<HspDigitized> digitalCopies = null;

    /* map hsp:Object */
    try {
      hspObject = mapHspObject(kodContent);
    } catch (IOException | DocumentException ex) {
      log.info("Error while mapping KOD, will skip processing message", ex);
      return null;
    }

    /* map hsp:Digitized */
    try {
      digitalCopies = mapHspDigitized(kodContent, hspObject.getId());
      digitalCopies.forEach(digi -> digi.setGroupIdSearch(hspObject.getId()));
    } catch (DocumentException | SAXException ex) {
      log.error("Error while mapping Digital Copy, will skip {}: {}", ex, ex.getMessage());
    }

    /* reset normdaten cache */
    if (normService != null) {
      normService.resetCache();
    }

    final String digitizedObject = CollectionUtils.isNotEmpty(digitalCopies) ? Boolean.TRUE.toString() : Boolean.FALSE.toString();

    final String digitizedIiifObject = (digitalCopies != null && digitalCopies.stream()
            .anyMatch(digi -> StringUtils.isNotBlank(digi.getManifestURIDisplay()))) ? Boolean.TRUE.toString() : Boolean.FALSE.toString();

    final List<HspDescription> descriptions = new ArrayList<>();

    for (byte[] descContent : descriptionContents) {
      try {
        CollectionUtils.addIgnoreNull(descriptions, mapHspDescription(descContent, hspObject, digitizedObject, digitizedIiifObject));
      } catch (DocumentException e) {
        log.warn("Error while processing hsp:description will skip {}: {}", e, e.getMessage());
      }
    }

    final String describedObject = CollectionUtils.isNotEmpty(descriptions) ? Boolean.TRUE.toString() : Boolean.FALSE.toString();

    hspObject.setDigitizedObjectFacet(digitizedObject);
    hspObject.setDigitizedIiifObjectFacet(digitizedIiifObject);
    hspObject.setDescribedObjectFacet(describedObject);

    hspObjectGroup.setHspObject(hspObject);
    hspObjectGroup.setHspDescriptions(descriptions);
    hspObjectGroup.setHspDigitized(digitalCopies);
    hspObjectGroup = injectFacetValuesJson(hspObjectGroup);

    return hspObjectGroup;
  }

  public HspObject mapHspObject(final byte[] tei) throws IOException, DocumentException {
    final HspObject obj = mapHspObjectByAnnotation(HspObject.class, tei);
    if (obj != null) {
      obj.setGroupIdSearch(obj.getId());
      obj.setMsIdentifierSearch(getMsIdentifierFromSettlementRepositoryIdno(obj));
      obj.setMsIdentifierSort(getMsIdentifierFromSettlementRepositorySortKeyOrIdno(obj));
      if (ArrayUtils.isNotEmpty(obj.getOrigDateFromFacet())) {
        obj.setOrigDateFromSort(Collections.min(Arrays.asList(obj.getOrigDateFromFacet())));
      }
      if (ArrayUtils.isNotEmpty(obj.getOrigDateToFacet())) {
        obj.setOrigDateToSort(Collections.max(Arrays.asList(obj.getOrigDateToFacet())));
      }
      obj.setTeiDocumentDisplay(new String(tei));
    }
    return obj;
  }

  public HspDescription mapHspDescription(final byte[] tei, final HspObject obj, final String digitizedObject, final String digitizedIiifObject) throws DocumentException {
    final HspDescription desc = mapHspObjectByAnnotation(HspDescription.class, tei);
    if (desc != null) {
      desc.setGroupIdSearch(obj.getId());
      desc.setOrigDateFromSort(obj.getOrigDateFromSort());
      desc.setOrigDateToSort(obj.getOrigDateToSort());
      desc.setTeiDocumentDisplay(new String(tei));
      desc.setMsIdentifierSearch(getMsIdentifierFromSettlementRepositoryIdno(desc));
      desc.setMsIdentifierSort(obj.getMsIdentifierSort());
      desc.setDigitizedObjectFacet(digitizedObject);
      desc.setDigitizedIiifObjectFacet(digitizedIiifObject);
      /* as this is a description itself, we can simply set this to 'true' */
      desc.setDescribedObjectFacet(Boolean.TRUE.toString());
    }
    return desc;
  }

  public List<HspDigitized> mapHspDigitized(final byte[] tei, final String groupId) throws DocumentException, SAXException {
    final XPathEvaluator ev = new XPathEvaluator(tei);
    final List<String> surrogates = ev.gatherXML("//tei:surrogates/tei:bibl");
    final List<HspDigitized> ret = new ArrayList<>();

    for (String el : surrogates) {
      el = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + el;
      HspDigitized digitalCopy = mapHspObjectByAnnotation(HspDigitized.class, el.getBytes());
      digitalCopy.setGroupIdSearch(groupId);
      ret.add(digitalCopy);
    }
    return ret;
  }

  public <T> T mapHspObjectByAnnotation(final Class<T> clazz, final byte[] tei) throws DocumentException {
    final XPathEvaluator ev = new XPathEvaluator(tei);
    final T ret;
    try {
      ret = clazz.getDeclaredConstructor()
              .newInstance();
    } catch (InstantiationException | IllegalAccessException |
             IllegalArgumentException | InvocationTargetException |
             NoSuchMethodException | SecurityException ex) {
      log.warn("Error while instantiating class {} {}: {}", clazz.getName(), ex.getMessage(), ex);
      return null;
    }

    final Field[] fields = clazz.getDeclaredFields();

    for (Field field : fields) {
      final List<String> values = getFieldValue(ev, field);
      if (CollectionUtils.isNotEmpty(values)) {
        final String methodName = String.format("set%s", StringUtils.capitalize(field.getName()));
        final Method method = MethodUtils.getMatchingMethod(clazz, methodName, field.getType());
        try {
          method.invoke(ret, TypeHelper.castToJavaType(values, field));
        } catch (IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException ex) {
          log.warn("Error while invoking method {} {}: {}", methodName, ex.getMessage(), ex);
        }
      }
    }
    return ret;
  }

  public List<String> getFieldValue(final XPathEvaluator ev, final Field field) {
    final List<String> ret = new ArrayList<>();
    final Set<XMLSource> attributes = AnnotatedElementUtils.getMergedRepeatableAnnotations(field, XMLSource.class);

    List<XMLSource> nodes = attributes.stream()
            .sorted(Comparator.comparingInt(XMLSource::priority))
            .collect(Collectors.toList());
    Iterator<XMLSource> it = nodes.iterator();

    while (it.hasNext() && ret.isEmpty()) {
      final XMLSource xmlSource = it.next();
      final List<String> values = getAttributeFieldValue(ev, xmlSource);
      ret.addAll(values);
    }
    return ret;
  }

  public List<String> getAttributeFieldValue(final XPathEvaluator ev, final XMLSource node) {
    List<String> ret = new ArrayList<>();

    for (Selector selector : node.selectors()) {
      final List<Node> elements = ev.getElements(selector.isMultiValue(), selector.xPath());
      for (Node el : elements) {
        /* get value */
        String value = XPathEvaluator.getValue(el, selector);
        List<String> processed = performPostProcessing(selector, value, node);
        List<String> filtered = processed.stream()
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());

        /* add value if there is any, add default value otherwise */
        if (filtered.isEmpty()) {
          final Optional<Element> currentElement = XPathEvaluator.getElement(el);
          if (currentElement.isPresent()) {
            Optional<String> defaultValue = XPathEvaluator.getAttrOrText(currentElement.get(), selector.defaultValue());
            defaultValue.ifPresent(ret::add);
          }
        } else {
          ret.addAll(filtered);
        }

        /* add additional value */
        final Optional<Element> currentElement = XPathEvaluator.getElement(el);
        if (currentElement.isPresent()) {
          Optional<String> additionalValue = XPathEvaluator.getAttrOrText(currentElement.get(), selector.additionalValue());
          additionalValue.ifPresent(ret::add);
        }
      }
    }
    if (node.distinct()) {
      return ret.stream()
              .distinct()
              .collect(Collectors.toList());
    } else return ret;
  }

  /**
   * applies all {@link ProcessingUnit}s from {@link Selector#processingUnits()} to the given {@code value}
   *
   * @param selector the selector containing the processing unit information, related to the {@code value}
   * @param value    the value on which the processing should be applied on
   * @param node     containing additional context information that may be relevant for post processing
   * @return the processed value
   */
  private List<String> performPostProcessing(final Selector selector, final String value, final XMLSource node) {
    final List<ProcessingUnit> postProcessors = List.of(selector.processingUnits());
    return postProcessor.runPostProcessing(postProcessors, node, value, normService);
  }
}
