package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.resultMapper;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ResultMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.GNDEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

class ResultMapperTest {
  @Test
  void whenPostProcessorIsCalledWithDefaultMapper_thenAllAttributesAreReturned() {
    final GNDEntity entity = GNDEntity.builder()
        .withGndId("testId")
        .withPreferredName("preferredName")
        .withVariantName(new GNDEntity.Variant[]{new GNDEntity.Variant("variantName", "de")})
        .build();

    List<String> result = ResultMapper.DEFAULT.getMapper()
        .apply(List.of(entity));

    assertThat(result, hasSize(3));
    assertThat(result, containsInAnyOrder("testId", "preferredName", "variantName"));
  }
}
