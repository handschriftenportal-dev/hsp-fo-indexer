package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation;

import java.lang.annotation.*;

/**
 *
 */

@Inherited
@Repeatable(XMLSources.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface XMLSource {

  Selector[] selectors() default {};

  /**
   * indicates whether the values should be distinguished or not
   */
  boolean distinct() default true;
}
