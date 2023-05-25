package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.xml;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.util.StringHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.Selector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class XPathEvaluator {

  private static final SAXReader saxReader = new SAXReader();
  static {
    saxReader.setMergeAdjacentText(true);
    saxReader.setStripWhitespaceText(true);
    saxReader.setIncludeExternalDTDDeclarations(false);
    try {
      saxReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      saxReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
      saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    } catch(SAXException e) {
      log.warn("Error while configuring SAX reader. This may lead to serious security vulnerabilities");
    }
    saxReader.setValidation(false);
  }
  private final Document document;

  public XPathEvaluator(final byte[] tei) throws DocumentException {
    document = saxReader.read(new ByteArrayInputStream(tei));
  }

  /**
   * Get the {@code content's} element
   *
   * @param node the {@link Node} the get the element from
   * @return the {@code content} itself, if it is an instance of {@link Element}, the {@code content's} parent element otherwise
   */
  public static Optional<Element> getElement(final Node node) {
    if (Objects.isNull(node)) {
      return Optional.empty();
    }
    if (node instanceof DefaultElement) {
      return Optional.of((DefaultElement) node);
    } else {
      return Optional.of(node.getParent());
    }
  }

  /**
   * Get {@code element's} content based on {@link Selector#additionalValue()}, if there is any
   *
   * @param element  the element to check for the additional value
   * @param attrNameOrText an attribute's name or `text()`
   * @return an {@link Optional} containing the additional value, an empty {@code Optional} otherwise
   */
  public static Optional<String> getAttrOrText(final Element element, final String attrNameOrText) {
    String ret = null;
    if (StringUtils.isNotBlank(attrNameOrText)) {
      if ("text()".equals(attrNameOrText)) {
        ret = element.getText();
      } else {
        ret = element.attributeValue(attrNameOrText);
      }
      ret = StringHelper.removeWhitespaces(ret);
    }
    return Optional.ofNullable(ret);
  }

  public Optional<Node> gatherNode(final String xPath) {
    final List<Node> nodes = gatherNodes(xPath);
    return Optional.ofNullable(nodes.get(0));
  }

  public static List<String> getTextFieldValue(final List<DefaultElement> elements) {
    return elements.stream()
            .map(DefaultElement::getText)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
  }

  public List<Node> gatherNodes(final String xPathExpression) {
    try {
      final Optional<XPath> xPath = XPathProvider.getXPath(xPathExpression);
      if(xPath.isPresent()) {
        return xPath.get().selectNodes(document);
      }
    } catch (JaxenException ex) {
      log.warn("Error while evaluating xPath expression {} {}: {}", xPathExpression, ex.getMessage(), ex);
    }
    return Collections.emptyList();
  }

  public List<Node> getElements(final boolean isMultivalued, final String xPathExpression) {
    List<Node> temp = gatherNodes(xPathExpression);

    if (!isMultivalued && !temp.isEmpty()) {
      return List.of(temp.get(0));
    } else {
      return temp;
    }
  }

  /**
   * Extracts a {@link Node}'s value based on a {@link Selector}
   *
   * @param node  the {@code content} that contains the value
   * @param selector containing the information, where the value is located
   * @return the content's value
   */
  public static String getValue(final Node node, final Selector selector) {
    String result = "";
    if (node instanceof DefaultElement) {
      if (StringUtils.isNotEmpty(selector.attribute())) {
        if(((DefaultElement) node).attribute(selector.attribute()) != null) {
          result = ((DefaultElement) node).attribute(selector.attribute()).getValue();
        }
      } else {
        result = node.getText();
      }
    } else {
      result = node.getStringValue();
    }
    return result.trim();
  }

  public List<String> gatherXML(final String xPath) {
    final List<Node> els = gatherNodes(xPath);
    return els.stream().map(Node::asXML).collect(Collectors.toList());
  }
}