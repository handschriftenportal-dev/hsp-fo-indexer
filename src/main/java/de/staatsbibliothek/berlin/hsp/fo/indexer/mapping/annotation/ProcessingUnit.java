package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ResultMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes.IAttributePostProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 *
 */

@Repeatable(ProcessingUnits.class)
@Retention(RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ProcessingUnit {
  Class<? extends IAttributePostProcessor> processorClass();

  ResultMapper mapper() default ResultMapper.DEFAULT;
}
