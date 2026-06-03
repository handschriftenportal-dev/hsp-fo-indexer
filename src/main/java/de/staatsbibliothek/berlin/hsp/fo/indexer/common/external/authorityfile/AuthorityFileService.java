package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.IContentResolver;

/**
 * Authority File Service Interface. Use the org.springframework.stereotype.Service
 * annotation for selecting the actual implementation.
 */

public interface AuthorityFileService extends IContentResolver {

  GNDEntity[] findByIdOrName(final String Id, final String nodeLabel) throws Exception;

  void resetCache();
}
