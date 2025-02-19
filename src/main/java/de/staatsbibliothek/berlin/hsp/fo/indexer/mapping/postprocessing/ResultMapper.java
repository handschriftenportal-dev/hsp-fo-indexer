package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.GNDEntity;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents different methods to process a post processor's result
 */
public enum ResultMapper {
  /**
   * use all information of a given {@link GNDEntity}
   */
  ALL,
  /**
   * Alias for {@link ResultMapper#ALL}
   */
  DEFAULT,
  /**
   * use only the preferred of a given {@link GNDEntity}
   */
  PREFERRED_NAME,
  /**
   * use only the variant names of a given {@link GNDEntity}
   */
  VARIANT_NAME;

  /**
   * get all attributes of the given {@code GNDEntity}
   */
  private static final Function<Object, List<String>> getAll = result -> mapGNDEntity(result, GNDEntity::getAsList);

  /**
   * get only the preferred name of the given {@code GNDEntity}
   */
  private static final Function<Object, List<String>> getPreferredName = result -> mapGNDEntity(result, gndEntity -> List.of(gndEntity.getPreferredName()));
  /**
   * get only the variant names of the given {@code GNDEntity}
   */
  private static final Function<Object, List<String>> getVariantName = result -> mapGNDEntity(result, gndEntity -> Stream.of(gndEntity.getVariantName())
      .map(GNDEntity.Variant::getName)
      .collect(Collectors.toList()));

  /**
   * init the constant's mapping functions
   */
  static {
    ALL.mapper = getAll;
    DEFAULT.mapper = getAll;
    PREFERRED_NAME.mapper = getPreferredName;
    VARIANT_NAME.mapper = getVariantName;
  }

  private Function<Object, List<String>> mapper;

  ResultMapper() {
  }

  /**
   * Tries to cast the given {@link Object} to a {@code List} of {@link  GNDEntity}s
   *
   * @param obj the object containing the {@code GNDEntity}s
   * @return an {@link Optional} containing the first {@code GNDEntity}, an empty {@code Optional} otherwise
   */
  private static <T> Optional<T> getFirstEntity(final Object obj, Class<T> clazz) {
    if (Objects.nonNull(obj) && obj instanceof List && !((List<?>) obj).isEmpty() && clazz.isInstance(((List<?>) obj).get(0))) {
      return Optional.of(((List<T>) obj).get(0));
    } else {
      return Optional.empty();
    }
  }

  /**
   * applies a given {@link Function} to the first {@link GNDEntity} that may be contained in a given Object
   *
   * @param objectToMap the object that should be mapped
   * @param map         the mapping function that should be applied
   * @return a {@code List} containing the mapped result, an empty {@code List} otherwise
   */
  private static List<String> mapGNDEntity(final Object objectToMap, final Function<GNDEntity, List<String>> map) {
    final Optional<GNDEntity> gndEntity = getFirstEntity(objectToMap, GNDEntity.class);
    if (gndEntity.isPresent()) {
      return map.apply(gndEntity.get());
    } else {
      return Collections.emptyList();
    }
  }

  /**
   * Get the enum constant's mapper
   *
   * @return the mapper
   */
  public Function<Object, List<String>> getMapper() {
    return mapper;
  }
}
