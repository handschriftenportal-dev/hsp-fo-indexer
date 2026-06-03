package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation;

import java.lang.annotation.*;

/**
 *
 */

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ProcessingUnits {
  ProcessingUnit[] value();
}
