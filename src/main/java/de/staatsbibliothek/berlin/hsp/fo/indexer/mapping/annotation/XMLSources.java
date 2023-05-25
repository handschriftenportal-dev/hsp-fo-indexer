package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation;

import java.lang.annotation.*;

/**
 *
 */

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface XMLSources {
  XMLSource[] value();
}
