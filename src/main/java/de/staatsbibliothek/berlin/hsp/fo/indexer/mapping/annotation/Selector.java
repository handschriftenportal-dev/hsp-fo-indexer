package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An Annotation that can be used for activating xPath based mapping of TEI documents
 */

@Inherited
@Retention(RUNTIME)
public @interface Selector {
  /**
   * This can either be an attribute's name of the xPath() referenced element or `text()` to use the elements text node.
   *
   * @return the additional value to map, empty string if none is specified
   */
  String additionalValue() default "";

  /**
   * an attribute's name to use instead of `text()`of the {@link Selector#xPath()} expression
   *
   * @return the attribute's name to map, empty String if none is specified
   */
  String attribute() default "";

  /**
   * A default value, that should be used if xPath() evaluation and post-processing results in an empty value
   * This can either be an attribute's name of the xPath() referenced element or `text()` to use the elements text node.
   *
   * @return the default value to map, empty string if none is specified
   */
  String defaultValue() default "";

  /**
   * if {@link Selector#xPath()} evaluation results in multiple values and all of them should be used, isMultiValue should be true.
   * Otherwise, only the first value is considered
   *
   * @return {@code true} if multiple values should be used, {@code false} otherwise
   */
  boolean isMultiValue() default false;

  /**
   * One or more Processing units, that should be used to post process the resulting value
   *
   * @return the processing units
   */
  ProcessingUnit[] processingUnits() default {};

  /**
   * A valid xPath expression that should be used to parse one or many values from a given TEI document.
   * If {@link Selector#attribute()} is empty, the text node (text()) will be used by default.
   *
   * @return the xPath expression
   */
  String xPath() default "";
}