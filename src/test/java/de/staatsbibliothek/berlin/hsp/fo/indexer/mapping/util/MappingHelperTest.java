package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.util;

import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspDescription;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspObject;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;

public class MappingHelperTest {
  @Test
  void givenHspObject_whenGetMsIdentifierFromSettlementRepositoryIdNoIsCalled_thenMsIdentifierIsCorrect() {
    HspObject hspObject = new HspObject();
    hspObject.setIdnoSearch("MS 1234");
    hspObject.setSettlementDisplay("Leipzig");
    hspObject.setRepositoryDisplay("Universitätsbibliothek Leipzig");
    assertThat(MappingHelper.getMsIdentifierFromSettlementRepositoryIdno(hspObject), is("Leipzig, Universitätsbibliothek Leipzig, MS 1234"));
  }

  @Test
  void givenHspObjectWithoutRepository_whenGetMsIdentifierFromSettlementRepositoryIdnoIsCalled_thenNullIsReturned() {
    HspObject hspObject = new HspObject();
    hspObject.setIdnoSearch("MS 1234");
    hspObject.setSettlementDisplay("Leipzig");
    assertThat(MappingHelper.getMsIdentifierFromSettlementRepositoryIdno(hspObject), is(nullValue()));
  }

  @Test
  void givenHspObjectWithoutSettlement_whenGetMsIdentifierFromSettlementRepositoryIdnoIsCalled_thenNullIsReturned() {
    HspObject hspObject = new HspObject();
    hspObject.setIdnoSearch("MS 1234");
    hspObject.setRepositoryDisplay("Universitätsbibliothek Leipzig");
    assertThat(MappingHelper.getMsIdentifierFromSettlementRepositoryIdno(hspObject), is(nullValue()));
  }

  @Test
  void givenHspObjectWithoutIdno_whenGetMsIdentifierFromSettlementRepositoryIdnoIsCalled_thenNullIsReturned() {
    HspObject hspObject = new HspObject();
    hspObject.setRepositoryDisplay("Universitätsbibliothek Leipzig");
    hspObject.setSettlementDisplay("Leipzig");
    assertThat(MappingHelper.getMsIdentifierFromSettlementRepositoryIdno(hspObject), is(nullValue()));
  }

  @Test
  void givenHspObjectWithoutRepository_whenGetMsIdentifierFromSettlementRepositorySortKeyOrIdnoIsCalled_thenMsIdentifierIsCorrect() {
    HspObject hspObject = new HspObject();
    hspObject.setIdnoSearch("MS 1234");
    hspObject.setIdnoSortKey("Kiel_UB_Cod-Ms-Bord-001");
    hspObject.setSettlementDisplay("Leipzig");
    assertThat(MappingHelper.getMsIdentifierFromSettlementRepositorySortKeyOrIdno(hspObject), is(nullValue()));
  }

  @Test
  void givenHspObjectWithoutSettlement_whenGetMsIdentifierFromSettlementRepositorySortKeyOrIdnoIsCalled_thenMsIdentifierIsCorrect() {
    HspObject hspObject = new HspObject();
    hspObject.setIdnoSearch("MS 1234");
    hspObject.setIdnoSortKey("Kiel_UB_Cod-Ms-Bord-001");
    hspObject.setRepositoryDisplay("Universitätsbibliothek Leipzig");
    assertThat(MappingHelper.getMsIdentifierFromSettlementRepositorySortKeyOrIdno(hspObject), is(nullValue()));
  }

  @Test
  void givenHspObjectWithIdNoSortKey_whenGetMsIdentifierFromSettlementRepositorySortKeyOrIdnoIsCalled_thenMsIdentifierIsCorrect() {
    HspObject hspObject = new HspObject();
    hspObject.setIdnoSortKey("Kiel_UB_Cod-Ms-Bord-001");
    hspObject.setSettlementDisplay("Leipzig");
    hspObject.setRepositoryDisplay("Universitätsbibliothek Leipzig");
    assertThat(MappingHelper.getMsIdentifierFromSettlementRepositorySortKeyOrIdno(hspObject),
        is("Leipzig, Universitätsbibliothek Leipzig, Kiel_UB_Cod-Ms-Bord-001"));
  }

  @Test
  void givenHspObjectWithIdNo_whenGetMsIdentifierFromSettlementRepositorySortKeyOrIdnoIsCalled_thenMsIdentifierIsCorrect() {
    HspObject hspObject = new HspObject();
    hspObject.setIdnoSearch("Cod. ms. Bord. 1");
    hspObject.setSettlementDisplay("Leipzig");
    hspObject.setRepositoryDisplay("Universitätsbibliothek Leipzig");
    assertThat(MappingHelper.getMsIdentifierFromSettlementRepositorySortKeyOrIdno(hspObject),
        is("Leipzig, Universitätsbibliothek Leipzig, Cod. ms. Bord. 1"));
  }

  @Test
  void givenHspObjectWithIdNoAndIdnoSortKey_whenGetMsIdentifierFromSettlementRepositorySortKeyOrIdnoIsCalled_thenMsIdentifierIsCorrect() {
    HspObject hspObject = new HspObject();
    hspObject.setIdnoSearch("Cod. ms. Bord. 1");
    hspObject.setIdnoSortKey("Kiel_UB_Cod-Ms-Bord-001");
    hspObject.setSettlementDisplay("Leipzig");
    hspObject.setRepositoryDisplay("Universitätsbibliothek Leipzig");
    assertThat(MappingHelper.getMsIdentifierFromSettlementRepositorySortKeyOrIdno(hspObject),
        is("Leipzig, Universitätsbibliothek Leipzig, Kiel_UB_Cod-Ms-Bord-001"));
  }

  @Test
  void givenHspObjectWithValidFormerInformation_whenGetFormerMsIdentifiersFromSettlementRepositoryIdnoIsCalled_thenResultIsCorrect() {
    HspObject hspObject = new HspObject();
    hspObject.setFormerSettlementSearch(new String[]{"Leipzig", "Berlin"});
    hspObject.setInstitutionPreviouslyOwningSearch(new String[] {"Albertina", "Stabi"});
    hspObject.setFormerIdnoSearch(new String[] {"UBL 001", "STABI 001"});

    assertThat(MappingHelper.getFormerMsIdentifiersFromSettlementRepositoryIdno(hspObject), arrayContainingInAnyOrder("Leipzig, Albertina, UBL 001", "Berlin, Stabi, STABI 001"));
  }

  @Test
  void givenHspObjectWithInValidFormerInformation_whenGetFormerMsIdentifiersFromSettlementRepositoryIdnoIsCalled_thenResultIsCorrect() {
    HspObject hspObject = new HspObject();
    /* count of settlement does not match count of repository and idno */
    hspObject.setFormerSettlementSearch(new String[]{"Leipzig"});
    hspObject.setInstitutionPreviouslyOwningSearch(new String[] {"Albertina", "Stabi"});
    hspObject.setFormerIdnoSearch(new String[] {"UBL 001", "STABI 001"});

    assertThat(MappingHelper.getFormerMsIdentifiersFromSettlementRepositoryIdno(hspObject), is(nullValue()));
  }

  @Test
  void givenHspObjectWithPairwiseIdenticalOrigDateAndOrigDateTo_thenOrigDateWhenIsCorrect() {
    final HspDescription hspDescription = new HspDescription();
    hspDescription.setOrigDateFromSearch(new Integer[] {1900, 1920});
    hspDescription.setOrigDateToSearch(new Integer[] {1900, 1920});

    assertThat(MappingHelper.getOrigDateFromOrigDateFromAndOrigDateTo(hspDescription), arrayContainingInAnyOrder(1900, 1920));
  }

  @Test
  void givenHspObjectWithNotPairwiseIdenticalOrigDateAndOrigDateTo_thenOrigDateWhenIsCorrect() {
    final HspDescription hspDescription = new HspDescription();
    hspDescription.setOrigDateFromSearch(new Integer[] {1900, 1930});
    hspDescription.setOrigDateToSearch(new Integer[] {1900, 1920});

    assertThat(MappingHelper.getOrigDateFromOrigDateFromAndOrigDateTo(hspDescription), arrayContainingInAnyOrder(1900));
  }

  @Test
  void givenHspObjectWithMismatchingOrigDateAndOrigDateToCount_thenOrigDateWhenIsEmpty() {
    final HspDescription hspDescription = new HspDescription();
    hspDescription.setOrigDateFromSearch(new Integer[] {1900});
    hspDescription.setOrigDateToSearch(new Integer[] {1900, 1920});

    assertThat(MappingHelper.getOrigDateFromOrigDateFromAndOrigDateTo(hspDescription), arrayWithSize(0));
  }
}
