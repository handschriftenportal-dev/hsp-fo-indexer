package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

@Slf4j
public class InstanceFactory {
  private InstanceFactory() {}

  public static <T> Optional<T> createInstance(final Class<T> clazz) {
    try {
      return Optional.of(clazz.getDeclaredConstructor()
          .newInstance());
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
      log.warn("Error while instantiating class {} {}", clazz.getName(), ex.getMessage(), ex);
      return Optional.empty();
    }
  }
}
