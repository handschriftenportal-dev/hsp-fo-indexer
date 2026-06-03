package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.util;

import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspBase;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspObjectGroup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

@Slf4j
public class FacetHelper {

  private FacetHelper() {
  }

  private static final BiPredicate<String, Object> SOURCE_SELECTOR_FACET = (methodName, value) -> methodName.endsWith("Facet");
  private static final BiPredicate<String, Object> TARGET_SELECTOR_ALWAYS = (methodName, value) -> true;

  /**
   * Enriches the facets of the given HspObjectGroup by aggregating data from its components.
   * <p/>
   * This method collects all relevant objects (HspObject, HspDescriptions, HspDigitized)
   * into a unified list and aggregates data for methods ending with "Facet".
   *
   * @param hspObjectGroup The HspObjectGroup whose facets are to be enriched.
   */
  public static void enrichFacets(final HspObjectGroup hspObjectGroup) {
    // Collect all items from the HspObjectGroup into a single list
    final List<HspBase> items = collectItems(hspObjectGroup);

    // Aggregate arrays and enrich only "Facet" related methods
    PropertyAggregationHelper.aggregateProperties(items, items, SOURCE_SELECTOR_FACET, TARGET_SELECTOR_ALWAYS);
  }


  /**
   * Collects all items from the given HspObjectGroup into a unified list for processing.
   *
   * @param hspObjectGroup The HspObjectGroup to collect items from.
   * @return A list of HspBase objects representing all relevant components.
   */
  private static List<HspBase> collectItems(final HspObjectGroup hspObjectGroup) {
    final List<HspBase> items = new ArrayList<>();
    items.add(hspObjectGroup.getHspObject());
    CollectionUtils.addAll(items, CollectionUtils.emptyIfNull(hspObjectGroup.getHspDescriptions()));
    CollectionUtils.addAll(items, CollectionUtils.emptyIfNull(hspObjectGroup.getHspDigitized()));
    return items;
  }
}