package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.postprocessors.TestPostProcessor;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.postprocessors.TestSecondPostProcessor;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.postprocessors.TestThirdPostProcessor;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.ProcessingUnit;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.AnnotationHelper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

class PostProcessorTest {

  @Test
  void whenPostProcessingIsCalled_thenAllMatchingProcessingMethodsAreInvoked() throws Exception {
    final PostProcessor pp = new PostProcessor("de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.postprocessors", getClass().getClassLoader());
    final List<ProcessingUnit> postProcessors = List.of(AnnotationHelper.getProcessorInstance(TestPostProcessor.class, ResultMapper.DEFAULT), AnnotationHelper.getProcessorInstance(TestThirdPostProcessor.class, ResultMapper.DEFAULT), AnnotationHelper.getProcessorInstance(TestSecondPostProcessor.class, ResultMapper.DEFAULT));

    final List<String> processedValues = pp.runPostProcessing(postProcessors, null, "val1");

    assertThat(processedValues, is(notNullValue()));
    assertThat(processedValues, hasSize(1));
    assertThat(processedValues.get(0), is("val1 TestPostProcessor processed TestThirdPostProcessor processed TestSecondPostProcessor processed"));
  }
}