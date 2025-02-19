package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.postprocessors;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.IContentResolver;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ResultMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.attributes.IAttributePostProcessor;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.XMLSource;

import java.util.List;

public class TestPostProcessor implements IAttributePostProcessor {

  @Override
  public List<String> process(XMLSource source, List<String> values, ResultMapper resultMapper, IContentResolver... resolver) {
    return List.of(String.format("%s %s processed", values.get(0), this.getClass()
        .getSimpleName()));
  }

  /**
   * Method for checking, if name filter works as expected
   *
   * @param source       the attribute's underlying {@code XMLSource} annotation
   * @param values       the attributes mapped value
   * @param id the id of the authority file if not already provided by the {@code value}, can be {@code null}
   * @param resolver
   * @return the processed values
   */
  public List<String> processWithOtherName(XMLSource source, List<String> values, String id, IContentResolver resolver) {
    return List.of(String.format("%s %s processed2", values.get(0), this.getClass()
        .getSimpleName()));
  }
}
