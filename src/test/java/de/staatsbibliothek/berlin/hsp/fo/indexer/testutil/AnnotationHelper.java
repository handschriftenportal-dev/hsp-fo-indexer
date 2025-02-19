package de.staatsbibliothek.berlin.hsp.fo.indexer.testutil;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ResultMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes.IAttributePostProcessor;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.ProcessingUnit;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.Selector;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.XMLSource;

import java.lang.annotation.Annotation;

/**
 * helper class for creating annotation instances
 */

public class AnnotationHelper {

  public static XMLSource getXMLSource(final String xPath, final boolean multiValue) {
    return getXMLSource(new Selector[]{getSelector(xPath, multiValue)}, true);
  }

  public static XMLSource getXMLSource(final String xPath, final boolean multiValue, final boolean distinct) {
    return getXMLSource(new Selector[]{getSelector(xPath, multiValue)}, distinct);
  }

  public static XMLSource getXMLSource(final Selector[] selectors, final boolean distinct) {
    return new XMLSource() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return XMLSource.class;
      }

      @Override
      public Selector[] selectors() {
        return selectors;
      }

      @Override
      public boolean distinct() {
        return distinct;
      }
    };
  }

  public static ProcessingUnit getProcessorInstance(final Class<? extends IAttributePostProcessor> pc, ResultMapper mapper) {
    return new ProcessingUnit() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return ProcessingUnit.class;
      }

      @Override
      public Class<? extends IAttributePostProcessor> processorClass() {
        return pc;
      }

      @Override
      public ResultMapper mapper() {
        return mapper;
      }
    };
  }

  public static Selector getSelector(final String xPath, final boolean multiValue) {
    return getSelector(xPath, multiValue, null, null, null);
  }

  public static Selector getSelector(final String xPath, final boolean multiValue, final String attributeName, final String defaultValue, final String additionalValue) {
    return new Selector() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return Selector.class;
      }

      @Override
      public String xPath() {
        return xPath;
      }

      @Override
      public boolean isMultiValue() {
        return multiValue;
      }

      @Override
      public ProcessingUnit[] processingUnits() {
        return null;
      }

      @Override
      public String attribute() {
        return attributeName;
      }

      @Override
      public String defaultValue() {
        return defaultValue;
      }

      @Override
      public String additionalValue() {
        return additionalValue;
      }
    };
  }
}
