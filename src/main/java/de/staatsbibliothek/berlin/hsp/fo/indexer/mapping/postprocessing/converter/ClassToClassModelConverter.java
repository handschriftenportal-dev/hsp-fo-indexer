package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.converter;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.ProcessingUnit;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.Selector;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.XMLSource;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.model.*;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class ClassToClassModelConverter {
  public static ClassModel convert(final Class<?> clazz) {
    final Field[] fields = clazz.getDeclaredFields();
    return ClassModel
        .builder()
        .withFields(convertFieldsToClassFields(fields))
        .withType(clazz)
        .build();
  }

  private static List<ClassField> convertFieldsToClassFields (final Field[] fields) {
    return Arrays
        .stream(fields)
        .map(ClassToClassModelConverter::convertFieldToClassField)
        .toList();
  }

  private static ClassField convertFieldToClassField(final Field field) {
    final List<XMLSource> xmlSources = AnnotatedElementUtils.getMergedRepeatableAnnotations(field, XMLSource.class)
        .stream()
        .toList();

    return ClassField.builder()
        .withName(field.getName())
        .withType(field.getType())
        .withSources(convertXMLSourcesToFieldSources(xmlSources.toArray(new XMLSource[] {})))
        .build();
  }

  private static List<FieldSource> convertXMLSourcesToFieldSources(final XMLSource[] xmlSources) {
    return Arrays.stream(xmlSources)
        .map(ClassToClassModelConverter::convertXMLSourceToFieldSource)
        .toList();
  }

  private static FieldSource convertXMLSourceToFieldSource(final XMLSource xmlSource) {
    return FieldSource
        .builder()
        .withDistinct(xmlSource.distinct())
        .withFieldSelectors(convertSelectorsToFieldSelectors(xmlSource.selectors()))
        .build();
  }

  private static List<FieldSelector> convertSelectorsToFieldSelectors(final Selector[] selectors) {
    return Arrays
        .stream(selectors)
        .map(ClassToClassModelConverter::convertSelectorToFieldSelector)
        .toList();
  }

  private static FieldSelector convertSelectorToFieldSelector(final Selector selector) {
    return FieldSelector.builder()
        .withAdditionalValue(selector.additionalValue())
        .withAttribute(selector.attribute())
        .withDefaultValue(selector.defaultValue())
        .withIsMultiValue(selector.isMultiValue())
        .withFieldProcessingUnits(convertProcessingUnitsToFieldProcessingUnits(selector.processingUnits()))
        .withXPath(selector.xPath())
        .build();
  }

  private static List<FieldProcessingUnit>convertProcessingUnitsToFieldProcessingUnits(final ProcessingUnit[] processingUnits) {
    return Arrays
        .stream(processingUnits)
        .map(ClassToClassModelConverter::convertProcessingUnitToFieldProcessingUnit)
        .toList();
  }

  private static FieldProcessingUnit convertProcessingUnitToFieldProcessingUnit(final ProcessingUnit processingUnit) {
    return FieldProcessingUnit
        .builder()
        .withProcessorClass(processingUnit.processorClass())
        .withResultMapper(processingUnit.mapper())
        .build();
  }
}
