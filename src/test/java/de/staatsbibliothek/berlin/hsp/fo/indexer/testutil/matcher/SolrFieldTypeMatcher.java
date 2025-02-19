package de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;
import java.util.Objects;

public class SolrFieldTypeMatcher extends TypeSafeMatcher<String> {

  private final String dest;
  private static final List<String> TEXT_TYPES = List.of("string", "text", "textExact", "textStemmed", "textExactWithoutPunctuationMarks");
  private static final String BOOL_TYPE = "bool";
  private static final List<String> ENUM_TYPES = List.of("has-notation-enum", "illuminated-enum", "orig-date-type", "status-type-enum");
  private static final List<String> TEXTABLE_TYPES = List.of("year");

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
    return (
        BOOL_TYPE.equals(src) && BOOL_TYPE.equals(dest) ||
        ENUM_TYPES.contains(src) && ENUM_TYPES.contains(dest) ||
        TEXT_TYPES.contains(src) && TEXT_TYPES.contains(dest) ||
        TEXTABLE_TYPES.contains(src) && TEXT_TYPES.contains(dest)
        );
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
