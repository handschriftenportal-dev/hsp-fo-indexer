package de.staatsbibliothek.berlin.hsp.fo.indexer;

import com.fasterxml.jackson.core.type.TypeReference;
import de.staatsbibliothek.berlin.hsp.fo.indexer.api.IndexerHealthIndicator;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.Constants;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.PersistenceServiceException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.SchemaUpdateException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.kafka.KafkaMessageReceiver;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.nachweis.NachweisHttpAdapter;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.util.FileHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.CopyField;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.Field;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.FieldType;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.SchemaVersion;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.SchemaService;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.impl.HspObjectGroupServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.impl.SchemaVersionServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.beans.BindingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
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

  @Value("${solr.schema-version:#{null}}")
  private String schemaVersion;

  @Autowired
  private SchemaService schemaService;

  @Autowired
  private SchemaVersionServiceImpl schemaVersionService;

  @Autowired
  private HspObjectGroupServiceImpl groupService;

  @Autowired
  private NachweisHttpAdapter nachweisHttpAdapter;

  @Autowired
  private KafkaMessageReceiver messageHandler;

  @Override
  public void run(String... args) throws Exception {
    try {
      updateSolrSchema();
      messageHandler.start();
    } catch (PersistenceServiceException | SchemaUpdateException e) {
      log.warn("an error occurred while updating solr schema {}: {}", e.getMessage(), e);
      IndexerHealthIndicator.setUnhealthyException(e);
    }
  }

  private void updateSolrSchema() throws PersistenceServiceException, SchemaUpdateException, IOException {
    if (StringUtils.isBlank(schemaVersion)) {
      log.info("No schema version provided. Updating Solr schema will be skipped.");
      return;
    }
    Optional<SchemaVersion> optSchemaVersion = Optional.empty();
    try {
      optSchemaVersion = schemaVersionService.find();
    } catch (BindingException e) {
      /* the cause is most likely an oldish schema version format, so just ignore and continue updating the schema */
    }

    if (optSchemaVersion.isPresent() && StringUtils.isNotBlank(optSchemaVersion.get()
        .getCurrentVersion())) {
      final boolean isSchemaUpToDate = SchemaVersionServiceImpl.isUpToDate(optSchemaVersion.get()
          .getCurrentVersion(), schemaVersion);

      if (isSchemaUpToDate) {
        return;
      }
    }

    /* fetch relevant schema definitions */
    final List<String> fields = schemaService.getFields();
    final List<CopyField> copyFields = schemaService.getCopyFields();
    final List<String> fieldTypes = schemaService.getFieldTypes();
    final List<String> dynamicFields = schemaService.getDynamicFields();

    fields.removeAll(Constants.WHITELIST_FIELDS);
    fieldTypes.removeAll(Constants.WHITELIST_FIELD_TYPES);

    // get fields from fields.json
    final List<Field> newFields = FileHelper.fromLocation(new TypeReference<>() {
    }, "schema/fields.json");

    // get copy field rules from copy-fields.json
    final List<CopyField> copyFieldRules = FileHelper.fromLocation(new TypeReference<>() {
    }, "schema/copy-fields.json");

    // get field types from types.json
    final List<FieldType> newFieldTypes = FileHelper.fromLocation(new TypeReference<>() {
    }, "schema/types.json");

    /* type-search is needed for executing the deletion query properly */
    if (schemaService.fieldExists("type-search")) {
      log.info("deleting all documents ...");
      groupService.deleteAll();
    }
    schemaVersionService.remove();

    log.info("start updating solr schema");

    log.info("deleting copy field rules from schema ...");
    schemaService.deleteCopyFields(copyFields);

    log.info("deleting fields from schema ...");
    schemaService.deleteFields(fields);

    log.info("deleting dynamic fields from schema ...");
    schemaService.deleteDynamicFields(dynamicFields);

    log.info("deleting field types from schema ...");
    schemaService.deleteFieldTypes(fieldTypes);

    log.info("adding new field types to schema ...");
    schemaService.addFieldTypes(newFieldTypes);

    log.info("adding new fields to schema ...");
    schemaService.addFields(newFields);

    log.info("adding new copy field rules to schema ...");
    schemaService.addCopyFields(copyFieldRules);

    log.info("update schema version to {}", schemaVersion);
    schemaVersionService.save(new SchemaVersion(schemaVersion));

    nachweisHttpAdapter.triggerIndexing();
  }
}
