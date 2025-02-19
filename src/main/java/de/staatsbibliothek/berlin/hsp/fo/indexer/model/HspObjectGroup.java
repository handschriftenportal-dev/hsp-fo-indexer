package de.staatsbibliothek.berlin.hsp.fo.indexer.model;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

@Data
public class HspObjectGroup implements HspBase {

  private HspObject hspObject;

  private List<HspDescription> hspDescriptions;

  private List<HspDigitized> hspDigitized;

  @Override
  public String getId() {
    final String id;
    if(getHspObject() != null && !StringUtils.isBlank(getHspObject().getId())) {
      id = getHspObject().getId();
    } else {
      id = null;
    }
    return id;
  }
}
