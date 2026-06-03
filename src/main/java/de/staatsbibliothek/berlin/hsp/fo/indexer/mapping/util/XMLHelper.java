package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.util;

public class XMLHelper {
  private XMLHelper() {}

  public static String addXMLHeader(String xml) {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + xml;
  }
}
