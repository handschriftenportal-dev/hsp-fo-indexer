package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiPredicate;

@Slf4j
public class PropertyAggregationHelper {

  private PropertyAggregationHelper() {
  }

  /**
   * Aggregates data from source objects and applies it to target objects based on the provided
   * sourceSelector and targetSelector functions. Arrays and single values are supported for aggregation.
   * The aggregated values are only applied if the targetSelector condition is satisfied.
   *
   * @param sources        List of source objects to aggregate data from.
   * @param targets        List of target objects to apply aggregated data to.
   * @param sourceSelector Function that selects methods of the source objects to process based on their names.
   * @param targetSelector Function to filter whether the aggregated data should be set on the target,
   *                       based on the value returned by the corresponding getter of the target and its name.
   */
  public static void aggregateProperties(
      final List<?> sources,
      final List<?> targets,
      final BiPredicate<String, Object> sourceSelector,
      final BiPredicate<String, Object> targetSelector) {

    final Map<String, Set<Object>> aggregatedValuesMap = new HashMap<>();

    // Aggregate data from source objects
    aggregateSourceData(sources, sourceSelector, aggregatedValuesMap);

    // Apply aggregated data to target objects based on targetSelector
    applyAggregatedDataToTargets(targets, targetSelector, aggregatedValuesMap);
  }

  /**
   * Aggregates data from source objects and stores it in the map.
   * <p/>
   *
   * @param sources             List of source objects to aggregate data from.
   * @param sourceSelector      Function that selects methods to process based on their names.
   * @param aggregatedValuesMap Map to store aggregated data, keyed by method names.
   */
  private static void aggregateSourceData(
      final List<?> sources,
      final BiPredicate<String, Object> sourceSelector,
      final Map<String, Set<Object>> aggregatedValuesMap) {
    String propName;
    for (final Object source : sources) {
      for (final Method method : source.getClass().getDeclaredMethods()) {

        // Select relevant getter methods
        if (method.getName().startsWith("get")) {
          propName = method.getName().substring(3);
          try {
            final Object result = method.invoke(source);
            if (sourceSelector.test(propName, result) && result != null) {
              aggregatedValuesMap.computeIfAbsent(propName, key -> new LinkedHashSet<>())
                  .addAll(result.getClass().isArray()
                      ? Arrays.asList((Object[]) result)
                      : Collections.singleton(result));
            }
          } catch (final IllegalAccessException | InvocationTargetException e) {
            log.debug("Error invoking method {}: {}", method.getName(), e.getMessage());
          }
        }
      }
    }
  }

  /**
   * Applies the aggregated data to the target objects. The values are only set if the targetSelector
   * condition is satisfied based on the value returned by the corresponding getter of the target.
   *
   * @param targets             List of target objects to apply data to.
   * @param targetSelector      Function to filter whether the aggregated data should be set,
   *                            based on the value returned by the target's getter method.
   * @param aggregatedValuesMap Map containing the aggregated data.
   */
  private static void applyAggregatedDataToTargets(
      final List<?> targets,
      final BiPredicate<String, Object> targetSelector,
      final Map<String, Set<Object>> aggregatedValuesMap) {

    for (final Object target : targets) {
      for (final Map.Entry<String, Set<Object>> entry : aggregatedValuesMap.entrySet()) {
        final String propName = entry.getKey();
        final String getterName = generateGetterForProperty(propName);
        final String setterName = generateSetterForProperty(propName);

        try {
          final Method getter = target.getClass().getDeclaredMethod(getterName);
          final Method setter = findSetterMethod(target, setterName);

          if (setter != null) {
            // Get the current value from the target using the getter
            final Object currentValue = getter.invoke(target);

            // Check the targetSelector before applying the new value
            if (targetSelector.test(setterName, currentValue)) {
              final Object values = prepareValuesForSetter(currentValue, entry.getValue(), setter.getParameterTypes()[0]);
              if (values != null) {
                setter.invoke(target, values);
              }
            }
          }
        } catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
          // this error occurs if source and target properties differ and there is noe need to worry
          log.debug("Error processing target for getter {} or setter {}: {}", propName, setterName, e);
        }
      }
    }
  }

  /**
   * Prepares the new values for the setter, determining whether they should be passed as an array or a single value.
   *
   * @param currentValue The current value from the target object (can be null).
   * @param newValues    The aggregated new values to set.
   * @param paramType    The parameter type expected by the setter method.
   * @return The prepared value(s), either as an array or a single value, or null if no new values are available.
   */
  private static Object prepareValuesForSetter(final Object currentValue, final Set<Object> newValues, final Class<?> paramType) {
    if (paramType.isArray()) {
      return mergeValuesIntoArray(currentValue, newValues, paramType.getComponentType());
    } else if (!newValues.isEmpty()) {
      return newValues.iterator().next(); // Return the first value for non-array parameters
    }
    return null; // Return null if no new values are available
  }

  /**
   * Merges the current value and new values into an array of the specified component type.
   * Ensures no duplicates are included in the resulting array.
   *
   * @param currentValue  The current value from the target object (can be null).
   * @param newValues     The aggregated new values to merge.
   * @param componentType The component type of the target array.
   * @return A new array containing both current and new values, without duplicates.
   */
  private static Object mergeValuesIntoArray(final Object currentValue, final Set<Object> newValues, final Class<?> componentType) {
    final Set<Object> uniqueValues = new LinkedHashSet<>();

    // Add existing array elements to the set
    if (currentValue != null) {
      final int currentLength = Array.getLength(currentValue);
      for (int i = 0; i < currentLength; i++) {
        uniqueValues.add(Array.get(currentValue, i));
      }
    }

    // Add new values to the set (duplicates will be ignored automatically)
    uniqueValues.addAll(newValues);

    // Create the resulting array
    final Object resultArray = Array.newInstance(componentType, uniqueValues.size());
    int index = 0;
    for (final Object value : uniqueValues) {
      Array.set(resultArray, index++, value);
    }

    return resultArray;
  }

  /**
   * Finds the setter method for the given setter name.
   *
   * @param target     The target object to search for the setter.
   * @param setterName The setter method name.
   * @return The setter method, or null if not found.
   */
  private static Method findSetterMethod(final Object target, final String setterName) {
    for (final Method method : target.getClass().getDeclaredMethods()) {
      if (method.getName().equals(setterName)) {
        return method;
      }
    }
    return null;
  }

  /**
   * Generates a setter method name from a property name (e.g., "Values" -> "setValues").
   *
   * @param PropName The name of the property method in camel case.
   * @return The corresponding setter method name.
   */
  private static String generateSetterForProperty(final String PropName) {
    return "set" + PropName;
  }

  /**
   * Generates a getter method name from a property name (e.g., "Values" -> "setValues").
   *
   * @param PropName The name of the property method in camel case.
   * @return The corresponding setter method name.
   */
  private static String generateGetterForProperty(final String PropName) {
    return "get" + PropName;
  }
}