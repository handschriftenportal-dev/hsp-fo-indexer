package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.xml;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.util.StringHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.ProcessingUnit;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.Selector;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.XMLSource;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.model.FieldProcessingUnit;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.model.FieldSelector;
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
import org.springframework.lang.NonNull;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
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
  private final PostProcessingHandler postProcessingHandler;

  public XPathEvaluator(final PostProcessingHandler postProcessingHandler) {
    this.postProcessingHandler = postProcessingHandler;
  }

  /**
   * Get the {@code content's} element
   *
   * @param node the {@link Node} the get the element from
   * @return the {@code content} itself, if it is an instance of {@link Element}, the {@code content's} parent element otherwise
   */
  private static Element getElement(@NonNull final Node node) {
    if (node instanceof DefaultElement) {
      return (DefaultElement) node;
    } else {
      return node.getParent();
    }
  }

  /**
   * Get {@code element's} content based on {@link Selector#additionalValue()}, if there is any
   *
   * @param element  the element to check for the additional value
   * @param attrNameOrText an attribute's name or `text()`
   * @return an {@link Optional} containing the additional value, an empty {@code Optional} otherwise
   */
  private static Optional<String> getAttrOrText(final Element element, final String attrNameOrText) {
    String ret = null;
    if (StringUtils.isNotBlank(attrNameOrText)) {
      if ("text()".equals(attrNameOrText)) {
        ret = element.getText();
      } else {
        ret = element.attributeValue(attrNameOrText);
      }
      ret = StringHelper.collapseAndTrim(ret);
    }
    return Optional.ofNullable(ret);
  }

  private static List<Node> gatherNodes(final String xPathExpression, final byte[] tei) {
    try {
      Document document = saxReader.read(new ByteArrayInputStream(tei));
      final Optional<XPath> xPath = XPathProvider.getXPath(xPathExpression);
      if(xPath.isPresent()) {
        return xPath.get().selectNodes(document);
      }
    } catch (JaxenException ex) {
      log.warn("Error while evaluating xPath expression {} {}:", xPathExpression, ex.getMessage(), ex);
    } catch(DocumentException ex) {
      log.warn("Error while parsing xml", ex);
    }
    return Collections.emptyList();
  }

  public List<Node> getNodes(final boolean isMultivalued, final String xPathExpression, final byte[] tei) {
    List<Node> temp = gatherNodes(xPathExpression, tei);

    if (!isMultivalued && !temp.isEmpty()) {
      return List.of(temp.getFirst());
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
  public List<String> getValue(final Node node, final FieldSelector selector) {
    String result = "";
    if (node instanceof DefaultElement) {
      if (StringUtils.isNotEmpty(selector.getAttribute())) {
        if(((DefaultElement) node).attribute(selector.getAttribute()) != null) {
          result = ((DefaultElement) node).attribute(selector.getAttribute()).getValue();
        }
      } else {
        result = node.getText();
      }
    } else {
      result = node.getStringValue();
    }
    //ToDo consider to pass XMLSource instead of null
    return performPostProcessing(selector, result.trim(), null);
  }

  /**
   * Evaluates the given xPath selector on the given node
   * @param node the node the selector should be evaluated on
   * @param selector the selector to evaluate
   * @return the value
   */
  public static Optional<String> getValue(final Node node, final String selector) {
    final Element currentElement = XPathEvaluator.getElement(node);
    return XPathEvaluator.getAttrOrText(currentElement, selector);
  }


  public static List<String> gatherXML(final String xPath, final byte[] tei) {
    final List<Node> els = gatherNodes(xPath, tei);
    return els.stream().map(Node::asXML).collect(Collectors.toList());
  }

  /**
   * applies all {@link ProcessingUnit}s from {@link Selector#processingUnits()} to the given {@code value}
   *
   * @param selector the selector containing the processing unit information, related to the {@code value}
   * @param value    the value on which the processing should be applied on
   * @param node     containing additional context information that may be relevant for post-processing
   * @return the processed value
   */
  private List<String> performPostProcessing(final FieldSelector selector, final String value, final XMLSource node) {
    final List<FieldProcessingUnit> postProcessors = selector.getFieldProcessingUnits();
    if(this.postProcessingHandler != null) {
      return postProcessingHandler.execute(postProcessors, node, value);
    }
    else return Collections.emptyList();
  }
}