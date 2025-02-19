package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.util;

import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspObjectGroup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public class FacetHelper {
  public static HspObjectGroup enrichFacets(HspObjectGroup hspObjectGroup) {
    final List<Object> items = new ArrayList<>();
    items.add(hspObjectGroup.getHspObject());
    CollectionUtils.addAll(items, CollectionUtils.emptyIfNull(hspObjectGroup.getHspDescriptions()));
    CollectionUtils.addAll(items, CollectionUtils.emptyIfNull(hspObjectGroup.getHspDigitized()));
    aggregateArrays(items, "Facet");
    return hspObjectGroup;
  }

  public static <T> List<T> aggregateArrays(List<T> instances, String suffix) {
    if (instances.isEmpty()) {
      return instances;
    }

    Map<String, List<Object>> aggregatedValuesMap = new HashMap<>();

    for (T obj : instances) {
      for (Method method : obj.getClass().getDeclaredMethods()) {
        if (method.getName().startsWith("get") && method.getName().endsWith(suffix) && method.getReturnType().isArray()) {
          try {
            Object[] result = (Object[]) method.invoke(obj);
            if (result != null) {
              aggregatedValuesMap.computeIfAbsent(method.getName(), k -> new ArrayList<>()).addAll(Arrays.asList(result));
            }
          } catch (IllegalAccessException | InvocationTargetException ex) {
            log.debug("Error invoking getter method: {}. Reason: {}", method.getName(), ex.getMessage());
          }
        }
      }
    }

    for (T obj : instances) {
      for (Map.Entry<String, List<Object>> entry : aggregatedValuesMap.entrySet()) {
        String setterName = convertGetterToSetter(entry.getKey());
        List<Object> distinctValues = entry.getValue().stream().distinct().toList();
        if(distinctValues.isEmpty()) {
          continue;
        }
        Object array = Array.newInstance(distinctValues.getFirst().getClass(), distinctValues.size());
        for (int i = 0; i < distinctValues.size(); i++) {
          Array.set(array, i, distinctValues.get(i));
        }
        try {
          Method setter = obj.getClass().getDeclaredMethod(setterName, array.getClass());
          setter.invoke(obj, array);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
          log.debug("Setter method not found or could not be invoked: {}. Reason: {}", setterName, e.getMessage());
        }
      }
    }
    return instances;
  }

  private static String convertGetterToSetter(final String getterName) {
    return getterName.replace("get", "set");
  }
}
