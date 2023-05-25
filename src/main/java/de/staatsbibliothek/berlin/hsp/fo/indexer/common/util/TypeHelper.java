package de.staatsbibliothek.berlin.hsp.fo.indexer.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

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

  public static Object castToJavaType(final List<String> values, final Field field) {
    if (field.getType()
        .isArray()) {
      return castToArray(values, field.getType()
          .getComponentType(), field.getName());
    } else {
      // add spaces between fragments
      String joined = StringUtils.join(values, " ");
      return castToType(joined.replaceAll("\\s+", " "), field.getType()
          .getSimpleName(), field.getName());
    }
  }

  public static Object castToType(final String value, final String simpleClassName, final String fieldName) {
    if (value == null) {
      return null;
    }
    try {
      if (Boolean.class.getSimpleName()
          .equalsIgnoreCase(simpleClassName)) {
        return Boolean.valueOf(value);
      } else if (Float.class.getSimpleName()
          .equalsIgnoreCase(simpleClassName)) {
        return Float.valueOf(value.replace(",", "."));
      } else if (Integer.class.getSimpleName()
          .equalsIgnoreCase(simpleClassName)) {
        return Integer.valueOf(value);
      } else if (Date.class.getSimpleName()
          .equalsIgnoreCase(simpleClassName)) {
        return DateTypeHelper.parseDate(value, dateFormats)
            .orElse(null);
      } else {
        return value;
      }
    } catch (NumberFormatException ex) {
      log.warn("Error while converting value {} to {} for field {}: {}", value, simpleClassName, fieldName, ex.getMessage());
      return null;
    }
  }

  public static Object[] castToArray(final List<String> obj, final Class<?> clazz, final String fieldName) {
    try {
      Object[] ret = (Object[]) Array.newInstance(clazz, obj.size());
      for (int i = 0; i < obj.size(); i++) {
        Object castedObj = castToType(obj.get(i), clazz.getSimpleName(), fieldName);
        if (castedObj instanceof Optional && ((Optional<?>) castedObj).isPresent()) {
          castedObj = ((Optional) castedObj).orElse(null);
        }
        if (castedObj != null) {
          Array.set(ret, i, castedObj);
        }
      }
      return ArrayUtils.removeAllOccurrences(ret, null);
    } catch (Exception ex) {
      log.warn("Error while casting to {} with {}: {}", clazz.getName(), ex.getMessage(), ex);
      return new Object[0];
    }
  }
}
