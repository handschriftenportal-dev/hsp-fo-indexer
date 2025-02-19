package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ResultMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class YearPostProcessorTest {
  YearPostProcessor ypp;

  @BeforeEach
  void setUp() {
    this.ypp = new YearPostProcessor();
  }

  @Test
  void whenProcessingIsCalledWithExistingId_thenResultIsPreferredName() throws Exception {
    final List<String> values = ypp.process(null, List.of("-0014"), ResultMapper.DEFAULT, null);

    assertThat(values, is(notNullValue()));
    assertThat(values, contains("-0014"));
  }
}
