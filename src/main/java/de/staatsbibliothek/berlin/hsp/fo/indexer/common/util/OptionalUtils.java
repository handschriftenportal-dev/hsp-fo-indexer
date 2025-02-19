package de.staatsbibliothek.berlin.hsp.fo.indexer.common.util;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class OptionalUtils {

  private OptionalUtils() {}

  /**
   * Adds all non-empty values from the given Optionals to the specified list.
   *
   * <p>Only present values will be added, and empty Optionals will be ignored.</p>
   *
   * @param <T> The type of the elements in the list.
   * @param list The list to which non-empty values should be added.
   * @param optionals The Optionals to be checked, and their values added if present.
   * @throws NullPointerException If the list or the array of Optionals is null.
   */
  @SafeVarargs
  public static <T> void addIgnoreEmpty(@Nonnull final List<T> list, @Nonnull final Optional<T>... optionals) {
    Stream.of(optionals)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(list::add);
  }

  /**
   * Creates a new list containing all non-empty values from the given Optionals.
   *
   * <p>Only present values will be included in the list, and empty Optionals will be ignored.</p>
   *
   * @param <T> The type of the elements in the list.
   * @param optionals The Optionals to be checked, and their values added if present.
   * @return A new list containing all non-empty values from the Optionals.
   * @throws NullPointerException If the array of Optionals is null.
   */
  @SafeVarargs
  public static <T> List<T> createListIgnoreEmpty(@Nonnull final Optional<T>... optionals) {
    return Stream.of(optionals)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }
}
