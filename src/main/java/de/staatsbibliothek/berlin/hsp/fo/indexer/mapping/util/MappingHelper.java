package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.util;

import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspBaseDocument;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspObject;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;

import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static de.staatsbibliothek.berlin.hsp.fo.indexer.common.util.ArrayHelper.allHaveEqualLength;

public class MappingHelper {
  private MappingHelper() {}

  /**
   * Generates an identifier from a {@link HspObject} or {@link HspDescription} based on its settlement and repository
   *
   * @param hspObjectOrDesc the document to generate the identifier for
   * @return the generated identifier, if settlement or repository is null, the identifier will be null too
   */
  @Nullable
  private static String getMsIdentifierFromSettlementRepository(@Nonnull final HspBaseDocument hspObjectOrDesc) {
    if (StringUtils.isBlank(hspObjectOrDesc.getSettlementDisplay()) || StringUtils.isBlank(hspObjectOrDesc.getRepositoryDisplay())) {
      return null;
    }
    return String.join(", ", hspObjectOrDesc.getSettlementDisplay(), hspObjectOrDesc.getRepositoryDisplay());
  }

  /**
   * Generates an identifier from a {@link HspObject} or {@link HspDescription} based on its settlement, repository and idno
   *
   * @param hspObjectOrDesc the document to generate the identifier for
   * @return the generated identifier, if settlement or repository or idno is null, the identifier will be null too
   */
  @Nullable
  public static String getMsIdentifierFromSettlementRepositoryIdno(final HspBaseDocument hspObjectOrDesc) {
    final String msIdentifierBase = getMsIdentifierFromSettlementRepository(hspObjectOrDesc);
    if (StringUtils.isBlank(msIdentifierBase) || StringUtils.isBlank(hspObjectOrDesc.getIdnoSearch())) {
      return null;
    }
    return String.join(", ", msIdentifierBase, hspObjectOrDesc.getIdnoSearch());
  }

  /**
   * Generates an identifier from a {@link HspObject} or {@link HspDescription} based on its settlement, repository, idno, and sortKey
   *
   * @param hspObjectOrDesc the document to generate the identifier for
   * @return the generated identifier, if settlement or repository is null or idno and sortKey is null, the identifier will be null too
   */
  @Nullable
  public static String getMsIdentifierFromSettlementRepositorySortKeyOrIdno(final HspObject hspObjectOrDesc) {
    final String msIdentifierBase = getMsIdentifierFromSettlementRepository(hspObjectOrDesc);
    if (StringUtils.isBlank(msIdentifierBase) || (StringUtils.isBlank(hspObjectOrDesc.getIdnoSearch()) && StringUtils.isBlank(hspObjectOrDesc.getIdnoSortKey()))) {
      return null;
    }
    final String sortKeyOrIdno = StringUtils.isBlank(hspObjectOrDesc.getIdnoSortKey()) ? hspObjectOrDesc.getIdnoSearch() : hspObjectOrDesc.getIdnoSortKey();

    return String.join(", ", msIdentifierBase, sortKeyOrIdno);
  }

  /**
   * Generates an identifier from a {@link HspObject} based on its former settlements, former repositories and former idnos
   *
   * @param obj the {@link HspObject} to generate the identifier for
   * @return an array containing the generated identifiers, if the arrays containing the former information does not have the same length, null will be returned.
   * For generating the identifiers, the {@link #getFormerMsIdentifierFromSettlementRepositoryIdno} will be used triple wise
   */
  @Nullable
  public static String[] getFormerMsIdentifiersFromSettlementRepositoryIdno(final HspObject obj) {
    final String[] formerSettlements = obj.getFormerSettlementSearch();
    final String[] formerRepositories = obj.getInstitutionPreviouslyOwningSearch();
    final String[] formerIdnos = obj.getFormerIdnoSearch();
    String[] result = null;
    if (allHaveEqualLength(formerSettlements, formerRepositories, formerIdnos)) {
      result = new String[formerSettlements.length];
      for (int i = 0; i < formerSettlements.length; i++) {
        result[i] = getFormerMsIdentifierFromSettlementRepositoryIdno(formerSettlements[i], formerRepositories[i], formerIdnos[i]);
      }
    }
    return result;
  }

  /**
   * Generates an identifier from the given settlement, repository and idno
   *
   * @param settlement the settlement information
   * @param repository the settlement information
   * @param idno the idno information
   * @return the generated identifier, if settlement or repository or idno is null or empty, the identifier will be null too
   */
  @Nullable
  private static String getFormerMsIdentifierFromSettlementRepositoryIdno(final String settlement, final String repository, final String idno) {
    if (StringUtils.isNotBlank(settlement) && StringUtils.isNotBlank(repository) && StringUtils.isNotEmpty(idno)) {
      return String.join(", ", settlement, repository, idno);
    }
    return null;
  }

  /**
   * determines the orig dates by comparing orig-date-from and orig-date-to pairwise.
   *
   * @param hspDescription the description whose orig date is to be used
   * @return An array that contains all matching date values
   */
  public static Integer[] getOrigDateFromOrigDateFromAndOrigDateTo(final HspDescription hspDescription) {
    final Integer[] origDateFrom = hspDescription.getOrigDateFromSearch();
    final Integer[] origDateTo = hspDescription.getOrigDateToSearch();
    List<Integer> result = new ArrayList<>();

    if (origDateFrom != null && origDateTo != null && origDateFrom.length == origDateTo.length) {
      for (int i = 0; i < origDateFrom.length; i++) {
        if (origDateFrom[i].equals(origDateTo[i])) {
          result.add(origDateFrom[i]);
        }
      }
    }
    return result.toArray(Integer[]::new);
  }

  /**
   * Returns an extended repository string by combining the settlement and repository facets
   * of the given HspCatalog object. If both facets are present, they are concatenated with a comma.
   * If only one of the facets is present, only that facet is returned. If neither facet is available,
   * an empty string is returned.
   *
   * @param repository the repository
   * @param settlement the settlement
   * @return a string representing the extended repository, or an empty string if no valid facets are found
   */
  public static String getExtendedRepositoryFromRepositoryAndSettlement(final String repository, final String settlement) {
    String result = "";

    if (StringUtils.isNotBlank(settlement) && StringUtils.isNotBlank(repository)) {
      result = settlement + ", " + repository;
    } else if (StringUtils.isNotBlank(repository)) {
      result = repository;
    }

    return result;
  }
}
