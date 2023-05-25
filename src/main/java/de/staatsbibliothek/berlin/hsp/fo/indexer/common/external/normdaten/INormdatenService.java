package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.normdaten;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.IContentResolver;

/**
 * Normdaten Service Interface. Use the org.springframework.stereotype.Service
 * annotation for selecting the actual implementation.
 */

public interface INormdatenService extends IContentResolver {

  GNDEntity[] findByIdOrName(final String Id, final String nodeLabel) throws Exception;

  void resetCache();

  void resetCache(final String id);
}
