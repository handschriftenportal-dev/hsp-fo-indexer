package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.AuthorityFileService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.GNDEntity;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.GNDEntityType;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * Provides helper methods for attribute post-processing
 */
public class PostProcessingHelper {

  private PostProcessingHelper() {
  }

  /**
   * fetches Authority Files by URI and {@code GNDEntityType}
   *
   * @param authorityFileURI     the URI of the authority file respectively GNDEntity that should be fetched
   * @param type             of the authority file / GNDEntity
   * @param authorityFileService service to fetch authority files
   * @return An array containing all matched authority files
   */
  private static GNDEntity[] getAuthorityFiles(final String authorityFileURI, final GNDEntityType type, final AuthorityFileService authorityFileService) throws ContentResolverException {
    final ContentInformation contentInformation = new ContentInformation(type);
    return (GNDEntity[]) authorityFileService.resolve(authorityFileURI, contentInformation);
  }

  /**
   * Filters for an IAuthorityFileService instance
   *
   * @param resolver the elements that should be filtered
   * @return {@link Optional} containing an {@code IAuthorityFileService} instance, an empty {@code Optional} otherwise
   */
  private static Optional<AuthorityFileService> getAuthorityFileService(final IContentResolver... resolver) {
    return Arrays.stream(resolver)
        .filter(AuthorityFileService.class::isInstance)
        .map(AuthorityFileService.class::cast)
        .findFirst();
  }

  /**
   * fetches authority files by URI and {@code GNDEntityType}
   *
   * @param id the id of the authority file respectively GNDEntity that should be fetched
   * @param type         of the authority file / GNDEntity
   * @param resolver     resolvers to fetch authority files
   * @return a {@code List} of all matched authority files
   */
  public static List<GNDEntity> getAuthorityFiles(final String id, final GNDEntityType type, final IContentResolver... resolver) throws ContentResolverException {
    final Optional<AuthorityFileService> authorityFileService = PostProcessingHelper.getAuthorityFileService(resolver);
    final List<GNDEntity> result = new ArrayList<>();
    if (authorityFileService.isPresent()) {
      GNDEntity[] entities = PostProcessingHelper.getAuthorityFiles(id, type, authorityFileService.get());
      if (ArrayUtils.isNotEmpty(entities)) {
        result.addAll(List.of(entities));
      }
    }
    return result;
  }
}
