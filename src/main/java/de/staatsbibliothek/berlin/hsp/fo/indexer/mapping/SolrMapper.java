package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.AuthorityFileService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.util.OptionalUtils;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ContentResolverException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.PostProcessor;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.util.FacetHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.util.MappingHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.util.XMLHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.xml.PostProcessingHandler;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.xml.XMLMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.xml.XPathEvaluator;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Maps TEI to Solr entities
 */
@Component
@Slf4j
public class SolrMapper {
  private final PostProcessor postProcessor;
  private AuthorityFileService authorityFileService;
  private final XMLMapper mapper;

  public SolrMapper() {
    postProcessor = new PostProcessor("de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes", getClass().getClassLoader());
    PostProcessingHandler handler = ((postProcessors, node, value) -> postProcessor.runPostProcessing(postProcessors, node, value, authorityFileService));
    mapper = new XMLMapper(handler);
  }

  @Autowired
  public void setAuthorityFileService(final AuthorityFileService authorityFileService) {
    this.authorityFileService = authorityFileService;
  }

  public Optional<HspObjectGroup> mapHspObjectGroup(@Nullable final byte[] kodContent, List<byte[]> descriptionContents) throws ContentResolverException {
    if(Objects.isNull(kodContent)) {
      return Optional.empty();
    }

    HspObjectGroup hspObjectGroup = new HspObjectGroup();
    final HspObject hspObject;
    List<HspDigitized> digitizeds = null;

    /* map hsp:Object */
    try {
      hspObject = mapHspObject(kodContent).orElse(null);
    } catch (DocumentException ex) {
      log.warn("Error while mapping KOD, will skip processing message", ex);
      return Optional.empty();
    }

    /* map hsp:Digitizeds */
    try {
      digitizeds = mapHspDigitizeds(kodContent, hspObject.getId(), hspObject.getRepositoryIdFacet());
    } catch (DocumentException ex) {
      log.warn("Error while mapping Digital Copy, will skip {}: {}", ex, ex.getMessage());
    }

    final Boolean digitizedObject = CollectionUtils.isNotEmpty(digitizeds);

    final Boolean digitizedIIIFObject = CollectionUtils.emptyIfNull(digitizeds).stream()
            .anyMatch(digitized -> StringUtils.isNotBlank(digitized.getManifestURISearch()));

    /* map hsp:Descriptions */
    List<HspDescription> descriptions = new ArrayList<>();

    for (byte[] descContent : descriptionContents) {
      try {
        OptionalUtils.addIgnoreEmpty(descriptions, mapHspDescription(descContent, hspObject, digitizedObject, digitizedIIIFObject));
      } catch (DocumentException e) {
        log.warn("Error while mapping hsp:description will skip {}: {}", e, e.getMessage());
      }
    }

    /* reset authority file cache */
    if (authorityFileService != null) {
      authorityFileService.resetCache();
    }

    final Boolean describedObject = CollectionUtils.isNotEmpty(descriptions);

    hspObject.setDigitizedObjectSearch(digitizedObject);
    hspObject.setDigitizedIiifObjectSearch(digitizedIIIFObject);
    hspObject.setDescribedObjectSearch(describedObject);

    hspObjectGroup.setHspObject(hspObject);
    hspObjectGroup.setHspDescriptions(descriptions);
    hspObjectGroup.setHspDigitized(new ArrayList<>(digitizeds));
    hspObjectGroup = FacetHelper.enrichFacets(hspObjectGroup);

    return Optional.of(hspObjectGroup);
  }

  public Optional<HspObject> mapHspObject(final byte[] tei) throws DocumentException {
    final Optional<HspObject> hspObjectOpt = mapper.map(HspObject.class, tei);
    if (hspObjectOpt.isPresent()) {
      // is this really a reference to the optionals underlying object?
      final HspObject hspObject = hspObjectOpt.get();
      hspObject.setGroupIdSearch(hspObject.getId());
      hspObject.setMsIdentifierSearch(MappingHelper.getMsIdentifierFromSettlementRepositoryIdno(hspObject));
      hspObject.setMsIdentifierSort(MappingHelper.getMsIdentifierFromSettlementRepositorySortKeyOrIdno(hspObject));
      hspObject.setFormerMsIdentifierSearch(MappingHelper.getFormerMsIdentifiersFromSettlementRepositoryIdno(hspObject));
      if (ArrayUtils.isNotEmpty(hspObject.getOrigDateFromFacet())) {
        hspObject.setOrigDateFromSort(Collections.min(Arrays.asList(hspObject.getOrigDateFromFacet())));
      }
      if (ArrayUtils.isNotEmpty(hspObject.getOrigDateToFacet())) {
        hspObject.setOrigDateToSort(Collections.max(Arrays.asList(hspObject.getOrigDateToFacet())));
      }
      hspObject.setTeiDocumentDisplay(new String(tei));
    }
    return hspObjectOpt;
  }

  public Optional<HspDescription> mapHspDescription(final byte[] tei, final HspObject obj, final Boolean digitizedObject, final Boolean digitizedIiifObject) throws DocumentException {
    final Optional<HspDescription> hspDescriptionOpt = mapper.map(HspDescription.class, tei);
    if (hspDescriptionOpt.isPresent()) {
      final HspDescription hspDescription = hspDescriptionOpt.get();
      hspDescription.setGroupIdSearch(obj.getId());
      hspDescription.setOrigDateFromSort(obj.getOrigDateFromSort());
      hspDescription.setOrigDateToSort(obj.getOrigDateToSort());
      hspDescription.setTeiDocumentDisplay(new String(tei));
      hspDescription.setMsIdentifierSearch(MappingHelper.getMsIdentifierFromSettlementRepositoryIdno(hspDescription));
      hspDescription.setMsIdentifierSort(obj.getMsIdentifierSort());
      hspDescription.setDigitizedObjectSearch(digitizedObject);
      hspDescription.setDigitizedIiifObjectSearch(digitizedIiifObject);
      /* as this is a description itself, we can simply set this to 'true' */
      hspDescription.setDescribedObjectSearch(Boolean.TRUE);
      hspDescription.setOrigDateWhenSearch(MappingHelper.getOrigDateFromOrigDateFromAndOrigDateTo(hspDescription));
    }
    return hspDescriptionOpt;
  }

  public List<HspDigitized> mapHspDigitizeds(final byte[] tei, final String groupId, final String repositoryId) throws DocumentException {
    final List<String> surrogates = XPathEvaluator.gatherXML("//tei:surrogates/tei:bibl", tei);

    return surrogates.stream()
        .map(XMLHelper::addXMLHeader)
        .map(el -> mapper.map(HspDigitized.class, el.getBytes()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(digitized -> {
          digitized.setGroupIdSearch(groupId);
          digitized.setKodIdDisplay(groupId);
          digitized.setRepositoryIdFacet(repositoryId);
          return digitized;
        })
        .toList();
  }

  /**
   * Maps byte representation of a TEI document to a {@link HspCatalog} instance
   * @param tei the TEI document as byte array
   * @return an {@link Optional} containing the {@link HspCatalog}, an empty Optional otherwise
   */
  public Optional<HspCatalog> mapHspCatalog(final byte[] tei) {
      Optional<HspCatalog> hspCatalogOpt = mapper.map(HspCatalog.class, tei);
      postProcessHspCatalog(hspCatalogOpt, tei);
      return hspCatalogOpt;
  }

  private void postProcessHspCatalog(final Optional<HspCatalog> hspCatalogOpt, final byte[] tei) {
    if (hspCatalogOpt.isPresent()) {
      hspCatalogOpt.get().setTeiDocumentDisplay(new String(tei));
      hspCatalogOpt.get().setRepositoryFacet(MappingHelper.getExtendedRepositoryFromRepositoryAndSettlement(hspCatalogOpt.get().getRepositoryFacet(), hspCatalogOpt.get().getSettlementFacet()));
    }
  }
}
