package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes.IAttributePostProcessor;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.GNDEntityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper for passing additional processing information when using {@link IAttributePostProcessor}
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class ContentInformation {
  private GNDEntityType type;
}
