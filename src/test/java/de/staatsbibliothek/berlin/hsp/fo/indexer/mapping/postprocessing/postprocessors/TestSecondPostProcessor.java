package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.postprocessors;

import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.IContentResolver;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ResultMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.annotation.XMLSource;

import java.util.List;

public class TestSecondPostProcessor extends TestPostProcessor {

  @Override
  public List<String> process(XMLSource source, List<String> values, final ResultMapper mapper, IContentResolver... resolver) {
    return super.process(source, values, mapper, resolver);
  }
}
