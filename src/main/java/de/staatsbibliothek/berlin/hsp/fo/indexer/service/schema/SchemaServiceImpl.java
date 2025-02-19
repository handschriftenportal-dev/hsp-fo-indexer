package de.staatsbibliothek.berlin.hsp.fo.indexer.service.schema;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.SchemaUpdateException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.CopyField;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.Field;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.FieldType;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.schema.impl.SchemaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.schema.FieldTypeDefinition;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;
import org.apache.solr.common.SolrException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SchemaServiceImpl implements SchemaService {
  private final SolrClient solrClient;
  private final ObjectMapper objectMapper;
  @Value("${solr.core}")
  private String solrCore;

  public SchemaServiceImpl(final SolrClient solrClient) {
    this.solrClient = solrClient;
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public List<String> getFields() throws SchemaUpdateException {
    final SchemaRequest.Fields fieldsRequest = new SchemaRequest.Fields();
    try {
      final SchemaResponse.FieldsResponse fieldsResponse = fieldsRequest.process(solrClient, solrCore);

      return fieldsResponse.getFields()
          .stream()
          .map(field -> field.get("name")
              .toString())
          .collect(Collectors.toList());
    } catch (IOException | SolrServerException e) {
      throw new SchemaUpdateException(e.getMessage(), e, true, true);
    }
  }

  @Override
  public List<String> getFieldTypes() throws SchemaUpdateException {
    final SchemaRequest.FieldTypes typesRequest = new SchemaRequest.FieldTypes();
    try {
      final SchemaResponse.FieldTypesResponse typesResponse = typesRequest.process(solrClient, solrCore);
      return typesResponse.getFieldTypes()
          .stream()
          .map(fieldType -> fieldType.getAttributes()
              .get("name")
              .toString())
          .collect(Collectors.toList());
    } catch (IOException | SolrServerException e) {
      throw new SchemaUpdateException(e.getMessage(), e, true, true);
    }
  }

  @Override
  public List<CopyField> getCopyFields() throws SchemaUpdateException {
    final SchemaRequest.CopyFields copyFieldsRequest = new SchemaRequest.CopyFields();
    try {
      final SchemaResponse.CopyFieldsResponse typeResponse = copyFieldsRequest.process(solrClient, solrCore);

      return typeResponse.getCopyFields()
          .stream()
          .map(attr -> CopyField.builder()
              .source(attr.get("source")
                  .toString())
              .dest(attr.get("dest")
                  .toString())
              .build())
          .collect(Collectors.toList());
    } catch (IOException | SolrServerException e) {
      throw new SchemaUpdateException(e.getMessage(), e, true, true);
    }
  }

  @Override
  public List<String> getDynamicFields() throws SchemaUpdateException {
    final SchemaRequest.DynamicFields req = new SchemaRequest.DynamicFields();
    try {
      SchemaResponse.DynamicFieldsResponse response = req.process(solrClient, solrCore);
      return response.getDynamicFields()
          .stream()
          .map(dynField -> dynField.get("name")
              .toString())
          .collect(Collectors.toList());
    } catch (IOException | SolrServerException e) {
      throw new SchemaUpdateException(e.getMessage(), e, true, true);
    }
  }

  private void addField(final Field field) throws SolrServerException, IOException {
    final SchemaRequest.AddField req = new SchemaRequest.AddField(objectMapper.convertValue(field, new TypeReference<>() {
    }));
    req.process(solrClient, solrCore);
  }

  @Override
  public void addFields(final List<Field> fields) throws SchemaUpdateException {
    for (Field field : fields) {
      log.info("adding {}", field.getName());
      try {
        addField(field);
      } catch (IOException | SolrServerException e) {
        throw new SchemaUpdateException(e.getMessage(), e, true, true);
      }
    }
  }

  private void addCopyField(final CopyField copyField) throws SchemaUpdateException {
    final SchemaRequest.AddCopyField req = new SchemaRequest.AddCopyField(copyField.getSource(), List.of(copyField.getDest()));
    try {
      req.process(solrClient, solrCore);
    } catch (IOException | SolrServerException e) {
      throw new SchemaUpdateException(e.getMessage(), e, true, true);
    }
  }

  @Override
  public void addCopyFields(final List<CopyField> copyFields) throws SchemaUpdateException {
    for (CopyField copyField : copyFields) {
      addCopyField(copyField);
    }
  }

  private void addFieldType(final FieldType fieldType) throws SchemaUpdateException {
    final FieldTypeDefinition ftDef = new FieldTypeDefinition();
    ftDef.setAttributes(objectMapper.convertValue(fieldType, new TypeReference<>() {
    }));
    final SchemaRequest.AddFieldType req = new SchemaRequest.AddFieldType(ftDef);
    try {
      req.process(solrClient, solrCore);
    } catch (IOException | SolrServerException e) {
      throw new SchemaUpdateException(e.getMessage(), e, true, true);
    }
  }

  @Override
  public void addFieldTypes(final List<FieldType> fieldTypes) throws SchemaUpdateException {
    for (FieldType fieldType : fieldTypes) {
      addFieldType(fieldType);
    }
  }

  private void deleteField(final String field) throws SchemaUpdateException {
    log.info("deleting {}", field);
    final SchemaRequest.DeleteField req = new SchemaRequest.DeleteField(field);
    try {
      req.process(solrClient, solrCore);
    } catch (IOException | SolrServerException e) {
      throw new SchemaUpdateException(e.getMessage(), e, true, true);
    }
  }

  @Override
  public void deleteFields(final List<String> fields) throws SchemaUpdateException {
    for (String field : fields) {
      deleteField(field);
    }
  }

  private void deleteCopyField(final CopyField copyField) throws SchemaUpdateException {
    final SchemaRequest.DeleteCopyField req = new SchemaRequest.DeleteCopyField(copyField.getSource(), List.of(copyField.getDest()));
    try {
      req.process(solrClient, solrCore);
    } catch (IOException | SolrServerException e) {
      throw new SchemaUpdateException(e.getMessage(), e, true, true);
    }
  }

  @Override
  public void deleteCopyFields(final List<CopyField> copyFields) throws SchemaUpdateException {
    for (CopyField copyField : copyFields) {
      deleteCopyField(copyField);
    }
  }

  private void deleteFieldType(final String fieldType) throws SchemaUpdateException {
    final SchemaRequest.DeleteFieldType req = new SchemaRequest.DeleteFieldType(fieldType);
    try {
      req.process(solrClient, solrCore);
    } catch (IOException | SolrServerException e) {
      throw new SchemaUpdateException(e.getMessage(), e, true, true);
    }
  }

  @Override
  public void deleteFieldTypes(final List<String> fieldTypes) throws SchemaUpdateException {
    for (String fieldType : fieldTypes) {
      deleteFieldType(fieldType);
    }
  }

  private void deleteDynamicField(final String dynamicField) throws SchemaUpdateException {
    final SchemaRequest.DeleteDynamicField request = new SchemaRequest.DeleteDynamicField(dynamicField);
    try {
      request.process(solrClient, solrCore);
    } catch (IOException | SolrServerException e) {
      throw new SchemaUpdateException(e.getMessage(), e, true, true);
    }
  }

  @Override
  public void deleteDynamicFields(final List<String> dynamicFields) throws SchemaUpdateException {
    for (String dynamicField : dynamicFields) {
      deleteDynamicField(dynamicField);
    }
  }

  @Override
  public boolean fieldExists(final String name) throws SchemaUpdateException {
    final SchemaRequest.Field req = new SchemaRequest.Field(name);
    try {
      return req.process(solrClient, solrCore)
          .getField() != null;
    } catch (SolrException | SolrServerException e) {
      if (e.getMessage()
          .contains("No such path /schema/fields/" + name) || e.getMessage().contains(String.format("No such field [%s]", name))) {
        return false;
      } else {
        throw new SchemaUpdateException(e.getMessage(), e, true, true);
      }
    } catch (IOException e) {
      throw new SchemaUpdateException(e.getMessage(), e, true, true);
    }
  }
}