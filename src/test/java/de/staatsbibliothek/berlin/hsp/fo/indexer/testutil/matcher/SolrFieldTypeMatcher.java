package de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Objects;

public class SolrFieldTypeMatcher extends TypeSafeMatcher<String> {

  private final String dest;

  SolrFieldTypeMatcher(final String dest) {
    this.dest = dest;
  }

  public static Matcher<String> canBeCopiedInto(final String dest) {
    return new SolrFieldTypeMatcher(dest);
  }

  @Override
  protected boolean matchesSafely(final String src) {
    if (Objects.isNull(src) || Objects.isNull(dest)) {
      return false;
    }
    if (src.equals(dest)) {
      return true;
    }
    if ((src.equals("text") || src.equals("textExact") || src.equals("textStemmed")) && (dest.equals("string") || dest.equals("text") || dest.equals("textExact") || dest.equals("textStemmed"))) {
      return true;
    }
    return false;
  }

  /**
   * Generates a description of the object.  The description may be part of a
   * a description of a larger object of which this is just a component, so it
   * should be worded appropriately.
   *
   * @param description The description to be built or appended to.
   */
  @Override
  public void describeTo(Description description) {
    description.appendText("field types are compatible");
  }
}
