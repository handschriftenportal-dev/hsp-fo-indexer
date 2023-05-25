package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten.GNDEntity;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten.GNDEntityType;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten.INormdatenService;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * Provides helper methods for attribute post-processing
 */
public class PostProcessingHelper {

  private PostProcessingHelper() {
  }

  /**
   * fetches Normdatums by URI and {@code GNDEntityType}
   *
   * @param normdatumURI     the URI of the Normdatum respectively GNDEntity that should be fetched
   * @param type             of the Normdatum/GNDEntity
   * @param normdatenService service to fetch Normdatums
   * @return An array containing all matched Normdatums
   */
  private static GNDEntity[] getNormdatums(final String normdatumURI, final GNDEntityType type, final INormdatenService normdatenService) throws ContentResolverException {
    final ContentInformation contentInformation = new ContentInformation(type);
    return (GNDEntity[]) normdatenService.resolve(normdatumURI, contentInformation);
  }

  /**
   * Filters for an INormdatumSerive instance
   *
   * @param resolver the elements that should be filtered
   * @return {@link Optional} containing an {@code INormdatumService} instance, an empty {@code Optional} otherwise
   */
  private static Optional<INormdatenService> getNormdatenService(final IContentResolver... resolver) {
    return Arrays.stream(resolver)
        .filter(INormdatenService.class::isInstance)
        .map(INormdatenService.class::cast)
        .findFirst();
  }

  /**
   * fetches Normdatums by URI and {@code GNDEntityType}
   *
   * @param normdatumURI the URI of the Normdatum respectively GNDEntity that should be fetched
   * @param type         of the Normdatum/GNDEntity
   * @param resolver     resolvers to fetch Normdatums
   * @return a {@code List} of all matched Normdatums
   */
  public static List<GNDEntity> getNormdatumList(final String normdatumURI, final GNDEntityType type, final IContentResolver... resolver) throws ContentResolverException {
    final Optional<INormdatenService> normdatenService = PostProcessingHelper.getNormdatenService(resolver);
    final List<GNDEntity> result = new ArrayList<>();
    if (normdatenService.isPresent()) {
      GNDEntity[] entities = PostProcessingHelper.getNormdatums(normdatumURI, type, normdatenService.get());
      if (ArrayUtils.isNotEmpty(entities)) {
        result.addAll(List.of(entities));
      }
    }
    return result;
  }
}
