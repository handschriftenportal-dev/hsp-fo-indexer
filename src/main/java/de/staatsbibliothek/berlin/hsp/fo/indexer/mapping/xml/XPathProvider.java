package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.xml;

import lombok.extern.slf4j.Slf4j;
import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class XPathProvider {
  private static final Map<String, BaseXPath> cache = new HashMap<>();

  private XPathProvider() {}

  public static Optional<XPath> getXPath(final String xPathExpression) {
    BaseXPath xPath = cache.get(xPathExpression);
    if(!cache.containsKey(xPathExpression)) {
      try {
        xPath = new Dom4jXPath(xPathExpression);
        xPath.addNamespace("tei", "http://www.tei-c.org/ns/1.0");

      } catch(JaxenException ex) {
        log.warn("An error occurred while instantiating {}:", xPathExpression, ex);
        xPath = null;
      }
      cache.put(xPathExpression, xPath);
    }
    return Optional.ofNullable(xPath);
  }
}
