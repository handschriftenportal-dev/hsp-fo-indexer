package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.impl;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.GNDEntity;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.AuthorityFileRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class InMemoryAuthorityFileRepository implements AuthorityFileRepository {
  private final Map<String, GNDEntity[]> cache = new HashMap<>();

  /**
   * @param id
   * @param data
   */
  @Override
  public void add(String id, GNDEntity[] data) {
    cache.put(id, data);
  }

  /**
   * @param id
   * @return
   */
  @Override
  public boolean contains(String id) {
    return cache.containsKey(id);
  }

  /**
   *
   */
  @Override
  public void deleteAll() {
    cache.clear();
  }

  /**
   * @param id
   */
  @Override
  public GNDEntity[] get(String id) {
    return cache.get(id);
  }

  /**
   * @param id
   */
  @Override
  public void remove(String id) {
    cache.remove(id);
  }
}
