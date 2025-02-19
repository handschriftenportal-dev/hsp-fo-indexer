package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile;

public interface AuthorityFileRepository {
  void add(final String id, final GNDEntity[] data);
  boolean contains(final String id);
  void deleteAll();
  GNDEntity[] get(final String id);
  void remove(final String id);

}
