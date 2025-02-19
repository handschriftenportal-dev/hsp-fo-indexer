package de.staatsbibliothek.berlin.hsp.fo.indexer.service.schema;

import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.SchemaVersion;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.SchemaUpdateException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.SchemaVersionRepositoryImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.schema.impl.SchemaVersionService;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SchemaVersionServiceImpl implements SchemaVersionService {

  private final SchemaVersionRepositoryImpl solrRepository;

  public SchemaVersionServiceImpl(
      @Autowired final SchemaVersionRepositoryImpl repository) {
    this.solrRepository = repository;
  }

  /**
   * Compares to different version Strings by using (@link ComparableVersion.compareTo(Item) compareTo)
   *
   * @param version          the version String
   * @param versionToCompare another version String that {@code version} should be compared with
   * @return true if {@code version} is higher or equal to {@code versionToCompare}, false otherwise
   * @throws SchemaUpdateException
   */
  public static boolean isUpToDate(final String version, final String versionToCompare) throws SchemaUpdateException {
    final ComparableVersion expectedVersionComparable = new ComparableVersion(versionToCompare);
    final ComparableVersion currentVersionComparable = new ComparableVersion(version);
    final int comparisonResult = currentVersionComparable.compareTo(expectedVersionComparable);

    if (comparisonResult > 0) {
      throw new SchemaUpdateException(String.format("The current schema version is higher than the expected one. Expected version is: %s Current version is: %s", versionToCompare, version), null, false, true);
    } else return comparisonResult == 0;
  }

  @Override
  public boolean save(final SchemaVersion schemaVersion) throws PersistenceServiceException {
    return solrRepository.save(schemaVersion);
  }

  @Override
  public boolean remove() throws PersistenceServiceException {
    return solrRepository.remove(SchemaVersion.VERSIONING_DOCUMENT_ID);
  }

  @Override
  public Optional<SchemaVersion> find() throws PersistenceServiceException {
    return solrRepository.findById(SchemaVersion.VERSIONING_DOCUMENT_ID);
  }
}
