package de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.matcher;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class ArrayContainsElementsMatcher extends TypeSafeMatcher<String[]> {

  private final String[] expectedElements;

  public ArrayContainsElementsMatcher(String[] expectedElements) {
    this.expectedElements = expectedElements;
  }

  @Override
  protected boolean matchesSafely(String[] actualArray) {
    for (String expectedElement : expectedElements) {
      boolean found = false;
      for (String actualElement : actualArray) {
        if (actualElement.equals(expectedElement)) {
          found = true;
          break;
        }
      }
      if (!found) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("an array containing the elements ");
    description.appendValueList("[", ", ", "]", expectedElements);
  }

  @Override
  protected void describeMismatchSafely(String[] actualArray, Description mismatchDescription) {
    mismatchDescription.appendText("was ").appendValueList("[", ", ", "]", actualArray);
  }

  public static ArrayContainsElementsMatcher containsElements(String... expectedElements) {
    return new ArrayContainsElementsMatcher(expectedElements);
  }
}