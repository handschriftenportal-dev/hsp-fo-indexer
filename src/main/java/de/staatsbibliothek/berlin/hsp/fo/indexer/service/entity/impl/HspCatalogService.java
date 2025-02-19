package de.staatsbibliothek.berlin.hsp.fo.indexer.service.entity.impl;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspCatalog;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.RepositoryImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.entity.RepositoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HspCatalogService implements RepositoryService<HspCatalog> {
  private final RepositoryImpl<HspCatalog> hspCatalogRepository;

  @Autowired
  public HspCatalogService(RepositoryImpl<HspCatalog> hspCatalogRepository) {
    this.hspCatalogRepository = hspCatalogRepository;
  }

  /**
   * @param catalog
   * @throws PersistenceServiceException
   */
  @Override
  public void insert(HspCatalog catalog) throws PersistenceServiceException {
    if (catalog != null) {
      final String id = catalog.getId();

      if (hspCatalogRepository.findById(id).isEmpty()) {
        hspCatalogRepository.save(catalog);
      } else {
        throw new PersistenceServiceException(String.format("Cannot insert hsp:catalog with id %s because it's already there.", id));
      }
    }
    else {
      throw new PersistenceServiceException("Cannot add catalog, since its value is null");
    }
  }

  /**
   * @param catalog
   * @throws PersistenceServiceException
   */
  @Override
  public void insertOrUpdate(HspCatalog catalog) throws PersistenceServiceException {
    if (catalog != null) {
    hspCatalogRepository.save(catalog);
    }
    else {
      throw new PersistenceServiceException("Cannot update catalog, since its value is null");
    }
  }

  /**
   * @param id
   * @throws PersistenceServiceException
   */
  @Override
  public void deleteById(String id) throws PersistenceServiceException {
    hspCatalogRepository.deleteById(id);
  }

  /**
   * @throws PersistenceServiceException
   */
  @Override
  public void deleteAll() throws PersistenceServiceException {
    hspCatalogRepository.deleteAll();
  }
}
