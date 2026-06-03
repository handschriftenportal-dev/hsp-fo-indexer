package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.util.ReflectionHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.ProcessingUnit;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.XMLSource;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.model.FieldProcessingUnit;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes.IAttributePostProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.reflections.scanners.Scanners.MethodsAnnotated;
import static org.reflections.scanners.Scanners.MethodsSignature;
import static org.reflections.util.ReflectionUtilsPredicates.withName;

@Slf4j
public class PostProcessor {
  public static final String POST_PROCESSOR_METHOD_NAME = "process";
  private final String postProcessingPackage;
  private final Map<String, Method> methods;
  private final Reflections reflections;
  private final Map<String, Object> instances;

  public PostProcessor(final String postProcessingPackage, final ClassLoader classLoader) {
    this.postProcessingPackage = postProcessingPackage;
    this.reflections = new Reflections(new ConfigurationBuilder().forPackage(postProcessingPackage, classLoader)
        .addScanners(MethodsSignature, MethodsAnnotated));
    this.methods = initAttributeProcessMethods();
    this.instances = ReflectionHelper.createInstances(new ArrayList<>(methods.keySet()));

  }

  private Map<String, Method> initAttributeProcessMethods() {
    /*
     * gather parameter types of process method for being able to find
     * appropriate implementations
     */
    final Optional<Method> processMethodDeclaration = ReflectionHelper.getMethodByName(IAttributePostProcessor.class, POST_PROCESSOR_METHOD_NAME);

    /*
     * collect process methods and map them by using it's annotated attribute
     * names
     */
    if (processMethodDeclaration.isEmpty()) {
      log.warn("Unable to to find post method declaration. Will not be able to perform any post processing.");
      return Collections.emptyMap();
    }

    final Class<?>[] types = processMethodDeclaration.get()
        .getParameterTypes();
    return reflections.get(MethodsSignature.with(types)
            .as(Method.class)
            .filter(method -> !method.getDeclaringClass()
                .isInterface())
            .filter(withName(POST_PROCESSOR_METHOD_NAME)))
        .stream()
        .collect(Collectors.toMap(m -> m.getDeclaringClass()
            .getName(), m -> m));
  }

  /**
   * the name of the attribute that should be processed
   *
   * @param processingUnits a @{code List} containing all {@code ProcessingUnits}
   * @param xmlSource       the attribute's corresponding {@link XMLSource}
   * @param value           the attribute's values
   * @param resolver        resolves external content
   * @return the post processed values
   */
  public List<String> runPostProcessing(final List<FieldProcessingUnit> processingUnits, final XMLSource xmlSource, final String value, final IContentResolver... resolver) {
    /* get all post-processing methods for the given attribute name */
    List<String> result = value != null ? List.of(value) : Collections.emptyList();

    if (CollectionUtils.isEmpty(processingUnits)) {
      return result;
    }

    for (FieldProcessingUnit processingUnit : processingUnits) {
      /* get instance for the containing class */
      String className = String.format("%s.%s", postProcessingPackage, processingUnit.getProcessorClass()
          .getSimpleName());
      /* invoke method on class instance */
      Method processingMethod = methods.get(className);
      try {
        if (Objects.nonNull(processingMethod)) {
          result = (List) processingMethod.invoke(instances.get(className), xmlSource, result, processingUnit.getResultMapper(), resolver);
        }
      } catch (IllegalAccessException | IllegalArgumentException |
               InvocationTargetException e) {
        log.warn("Error while invoking {}.{}:", className, processingMethod.getName());
      }
    }
    return result;
  }
}
