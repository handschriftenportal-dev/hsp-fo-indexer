package de.staatsbibliothek.berlin.hsp.fo.indexer.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class TypeHelper {
  private static final List<SimpleDateFormat> dateFormats = new ArrayList<>();
  static {
    dateFormats.add(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH));
    dateFormats.add(new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH));
    dateFormats.add(new SimpleDateFormat("yyyy", Locale.ENGLISH));
  }

  private TypeHelper() {}

  /**
   *
   * @param values
   * @return
   */
  public static Optional<?> castToJavaType(final List<String> values, final Class clazz) {
    if (clazz.isArray()) {
      return Optional.of(castToArray(values, clazz.getComponentType()));
    } else {
      String joined = StringUtils.join(values, " ");
      return castToType(StringHelper.collapseSpaces(joined), clazz.getSimpleName());
    }
  }

  private static Optional<?> castToType(@NonNull final String value, final String simpleClassName) {
    Optional<?> result;
    try {
      if (Boolean.class.getSimpleName().equalsIgnoreCase(simpleClassName)) {
        result = Optional.of(Boolean.valueOf(value));
      } else if (Float.class.getSimpleName().equalsIgnoreCase(simpleClassName)) {
        result = Optional.of(Float.valueOf(value.replace(",", ".")));
      } else if (Integer.class.getSimpleName().equalsIgnoreCase(simpleClassName)) {
        result = Optional.of(Integer.valueOf(value));
      } else if (Date.class.getSimpleName().equalsIgnoreCase(simpleClassName)) {
        result = DateTypeHelper.parseDate(value, dateFormats);
      }
      else {
        result = Optional.of(value);
      }
    } catch (NumberFormatException ex) {
      log.warn("Error while converting value {} to {}: {}", value, simpleClassName, ex.getMessage());
      result = Optional.empty();
    }
    return result;
  }

  private static Object[] castToArray(final List<String> obj, final Class<?> clazz) {
    try {
      Object[] ret = (Object[]) Array.newInstance(clazz, obj.size());
      for (int i = 0; i < obj.size(); i++) {
        Optional<?> castedObj = castToType(obj.get(i), clazz.getSimpleName());
        if (castedObj.isPresent()) {
          Array.set(ret, i, castedObj.get());
        }
      }
      return ArrayUtils.removeAllOccurrences(ret, null);
    } catch (Exception ex) {
      log.warn("Error while casting to {} with {}", clazz.getName(), ex.getMessage(), ex);
      return new Object[0];
    }
  }
}
