package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.xml;

import org.dom4j.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.fixture.FileFixture.dataFromResourceFilename;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

@DisplayName("xPath Expression Evaluation")
public class XPathEvaluatorTest {

  private XPathEvaluator xPathEvaluator;

  public XPathEvaluatorTest() {
    xPathEvaluator = new XPathEvaluator((postProcessors, node, value) -> List.of(value));
  }

  @Test
  @DisplayName("Wrong xPath")
  void testGatherMultipleNodesByXPathExpression_NoElements() throws Exception {
    final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
    final String xPathExpression = "//tei:teiHeader2";
    final List<Node> nodes = xPathEvaluator.getNodes(false, xPathExpression, doc);

    assertThat(nodes, hasSize(0));
  }

  @Test
  @DisplayName("Multiple text nodes")
  void testGatherMultipleNodesByXPathExpression_SimpleText() throws Exception {
    final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
    final String xPathExpression = "//tei:teiHeader/tei:fileDesc/tei:editionStmt/tei:respStmt/tei:name";

    final List<Node> nodes = xPathEvaluator.getNodes(true, xPathExpression, doc);

    assertThat(nodes, hasSize(2));
    assertThat(nodes.get(0).getText(), equalTo("Bertram Lesser"));
    assertThat(nodes.get(1).getText(), equalTo("Herzog August Bibliothek"));
  }

  @Test
  @DisplayName("Text node with condition")
  void testGatherNodeByXPathExpression_AttributeCondition() throws Exception {
    final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
    final String xPathExpression = "//tei:teiHeader/tei:fileDesc/tei:editionStmt/tei:respStmt/tei:name[@type=\"org\"]";

    final List<Node> nodes = xPathEvaluator.getNodes(false, xPathExpression, doc);

    assertThat(nodes, hasSize(1));
    assertThat(nodes.get(0).getText(), equalTo("Herzog August Bibliothek"));
  }

  @Test
  @DisplayName("Text node")
  void testGatherNodeByXPathExpression_simpleText() throws Exception {
    final byte[] doc = dataFromResourceFilename("fixtures/tei_odd.xml");
    final String xPathExpression = "//tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title";

    final List<Node> nodes = xPathEvaluator.getNodes(false, xPathExpression, doc);

    assertThat(nodes, hasSize(1));
    assertThat(nodes.get(0).getText(), equalTo("Beschreibung von Cod. Guelf. 277 Helmst. (Die mittelalterlichen Helmstedter Handschriften der Herzog August Bibliothek. Teil 2: Cod. Guelf. 277 bis 370 Helmst. und Helmstedter Fragmente. Beschrieben von Bertram Lesser. Wiesbaden: Harrassowitz, (im Erscheinen).)"));
  }
}
