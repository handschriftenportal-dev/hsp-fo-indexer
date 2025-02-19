package de.staatsbibliothek.berlin.hsp.fo.indexer;

import com.fasterxml.jackson.core.type.TypeReference;
import de.staatsbibliothek.berlin.hsp.fo.indexer.api.IndexerHealthIndicator;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.Constants;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.HSPException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.SchemaUpdateException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.kafka.KafkaMessageReceiver;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.nachweis.DocumentType;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.nachweis.NachweisHttpAdapter;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.util.FileHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.HspObjectGroup;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.CopyField;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.Field;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.FieldType;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.SchemaVersion;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.entity.RepositoryService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.entity.impl.HspCatalogService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.replication.ReplicationAdminService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.schema.impl.SchemaService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.schema.impl.SchemaVersionService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.schema.SchemaVersionServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.beans.BindingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 *
 */
@Component
@Slf4j
public class HspFoIndexerCommandLineRunner implements CommandLineRunner {
  private RepositoryService<HspObjectGroup> groupService;

  private NachweisHttpAdapter nachweisHttpAdapter;

  private KafkaMessageReceiver kafkaMessageReceiver;

  private ReplicationAdminService replicationAdminService;

  private SchemaService schemaService;

  @Value("${solr.schema-version:#{null}}")
  private String schemaVersion;

  private SchemaVersionService schemaVersionService;

  private boolean triggerReindexing;
  private HspCatalogService hspCatalogService;

  public HspFoIndexerCommandLineRunner(
      @Autowired final RepositoryService<HspObjectGroup> groupService,
      @Autowired final KafkaMessageReceiver kafkaMessageReceiver,
      @Autowired final NachweisHttpAdapter nachweisHttpAdapter,
      @Autowired final ReplicationAdminService replicationAdminService,
      @Autowired final SchemaService schemaService,
      @Autowired final SchemaVersionService schemaVersionService
      ) {
    this.groupService = groupService;
    this.kafkaMessageReceiver = kafkaMessageReceiver;
    this.nachweisHttpAdapter = nachweisHttpAdapter;
    this.replicationAdminService = replicationAdminService;
    this.schemaService = schemaService;
    this.schemaVersionService = schemaVersionService;
  }

  @Autowired
  public void setTriggerReindexing(@Value("${solr.trigger-reindexing: false}") final boolean triggerReindexing) {
    this.triggerReindexing = triggerReindexing;
  }

  @Autowired
  public void setHspCatalogService(HspCatalogService hspCatalogService) {
    this.hspCatalogService = hspCatalogService;
  }

  @Scheduled(fixedRateString = "${common.restart-interval}")
  public void restart() {
    if (!IndexerHealthIndicator.isHealthy() &&
        IndexerHealthIndicator.getCriticalException() != null &&
        IndexerHealthIndicator.getCriticalException().isCausedByDependence()) {
      IndexerHealthIndicator.resetException();
      start();
    }
  }

  @Override
  public void run(String... args) {
    start();
  }

  private void start() {
    try {
      updateSolrSchema();
      kafkaMessageReceiver.start();
    } catch (HSPException e) {
      log.error("an error occurred while updating solr schema {}", e.getMessage(), e);
      if(e.isCritical()) {
        IndexerHealthIndicator.setCriticalException(e);
      }
    }
  }

  private void updateSolrSchema() throws HSPException {
    if (!isSchemaUpdateNecessary()) {
      return;
    }
    log.info("disabling replication");
    replicationAdminService.disablePolling();

    log.info("delete data");
    /* type-search is needed for executing the deletion query properly */
    if (schemaService.fieldExists("type-search")) {
      log.info("deleting all documents ...");
      groupService.deleteAll();
      hspCatalogService.deleteAll();
    }
    removeSchemaDefinition();
    addSchemaDefinition();
    if(triggerReindexing) {
      nachweisHttpAdapter.triggerIndexing(DocumentType.ALL);
    }
  }

  private boolean isSchemaUpdateNecessary() throws PersistenceServiceException, SchemaUpdateException {
    if (StringUtils.isBlank(schemaVersion)) {
      log.info("No schema version provided. Updating Solr schema will be skipped.");
      return false;
    }
    Optional<SchemaVersion> optSchemaVersion = Optional.empty();
    try {
      optSchemaVersion = schemaVersionService.find();
    } catch (BindingException e) {
      /* the cause is most likely an oldish schema version format, so just ignore and continue updating the schema */
    }

    if (optSchemaVersion.isPresent() &&
        StringUtils.isNotBlank(optSchemaVersion.get().getCurrentVersion())) {
      return !SchemaVersionServiceImpl.isUpToDate(optSchemaVersion.get()
          .getCurrentVersion(), schemaVersion);
    }
    return true;
  }

  private void removeSchemaDefinition() throws SchemaUpdateException, PersistenceServiceException {
    schemaVersionService.remove();

    /* fetch relevant schema definitions */
    final List<String> fields = schemaService.getFields();
    final List<CopyField> copyFields = schemaService.getCopyFields();
    final List<String> fieldTypes = schemaService.getFieldTypes();
    final List<String> dynamicFields = schemaService.getDynamicFields();

    fields.removeAll(Constants.WHITELIST_FIELDS);
    fieldTypes.removeAll(Constants.WHITELIST_FIELD_TYPES);

    log.info("deleting copy field rules from schema ...");
    schemaService.deleteCopyFields(copyFields);

    log.info("deleting fields from schema ...");
    schemaService.deleteFields(fields);

    log.info("deleting dynamic fields from schema ...");
    schemaService.deleteDynamicFields(dynamicFields);

    log.info("deleting field types from schema ...");
    schemaService.deleteFieldTypes(fieldTypes);
  }

  private void addSchemaDefinition() throws SchemaUpdateException, PersistenceServiceException {
    final List<Field> newFields;
    final List<CopyField> newCopyFieldRules;
    final List<FieldType> newFieldTypes;

    try {
      // load field definition from fields.json
      newFields = FileHelper.fromLocation(new TypeReference<>() {
      }, "schema/fields.json");

      // load copy field definition from copy-fields.json
      newCopyFieldRules = FileHelper.fromLocation(new TypeReference<>() {
      }, "schema/copy-fields.json");

      // load field type definition from types.json
      newFieldTypes = FileHelper.fromLocation(new TypeReference<>() {
      }, "schema/types.json");
    } catch (IOException e) {
      throw new SchemaUpdateException("Error loading schema definition", e);
    }

    log.info("adding new field types to schema ...");
    schemaService.addFieldTypes(newFieldTypes);

    log.info("adding new fields to schema ...");
    schemaService.addFields(newFields);

    log.info("adding new copy field rules to schema ...");
    schemaService.addCopyFields(newCopyFieldRules);

    schemaVersionService.save(new SchemaVersion(schemaVersion));
    log.info("updated schema to version {}", schemaVersion);
  }
}
