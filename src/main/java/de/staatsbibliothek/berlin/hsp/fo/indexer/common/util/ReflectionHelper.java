package de.staatsbibliothek.berlin.hsp.fo.indexer.common.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
@Slf4j
public class ReflectionHelper {

  private ReflectionHelper() {
  }

  /**
   * Returns a {@code Method} by its name
   *
   * @param clazz      the class that holds the method
   * @param methodName the method's name
   * @return an {@code Optional} containing the method, an empty {@code Optional} otherwise
   */
  public static Optional<Method> getMethodByName(final Class<?> clazz, final String methodName) {
    return Stream.of(clazz.getMethods())
        .filter(m -> m.getName()
            .equals(methodName))
        .findFirst();
  }

  /**
   * Creates an instance of the given class.
   *
   * @param className the name of the class that should be instanced
   * @return An {@code Optional} containing the object, an empty {@code Optional} in case of an error
   */
  public static <T> Optional<T> createInstance(final String className) {
    T instance = null;
    try {
      Class<T> clazz = (Class<T>) Class.forName(className);
      instance = clazz.getConstructor()
          .newInstance();
    } catch (InstantiationException | IllegalAccessException |
             IllegalArgumentException | InvocationTargetException |
             NoSuchMethodException | SecurityException |
             ClassNotFoundException e) {
      log.warn("An error occurred while instantiating  class {} {}: {}", className, e.getMessage(), e);
    }
    return Optional.ofNullable(instance);
  }

  /**
   * Creates a map of objects. These objects are instanced by the given class names.
   * The className is used as key, the instance as value. Duplicates will be erased
   *
   * @param classes {@code List} of classNames that should be instanced
   * @return a new {@code Map} containing the created objects
   */
  public static Map<String, Object> createInstances(final List<String> classes) {

    /* create instances and map them by using its class names */
    return classes.stream()
        .map(ReflectionHelper::createInstance)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toMap(instance -> instance.getClass()
            .getName(), instance -> instance, (k1, k2) -> k1));
  }
}