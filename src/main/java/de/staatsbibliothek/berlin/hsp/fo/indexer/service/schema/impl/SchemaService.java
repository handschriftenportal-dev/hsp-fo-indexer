package de.staatsbibliothek.berlin.hsp.fo.indexer.service.schema.impl;

import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.CopyField;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.Field;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.FieldType;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.SchemaUpdateException;

import java.util.List;

/**
 * Provides information about a Solr core's schema
 */
public interface SchemaService {
  /**
   * get all fields
   *
   * @return list of field names
   * @throws SchemaUpdateException
   */
  List<String> getFields() throws SchemaUpdateException;

  /**
   * checks if a field already exists
   *
   * @param name the field's name to check
   * @return true if the field exists, false otherwise
   * @throws SchemaUpdateException
   */
  boolean fieldExists(final String name) throws SchemaUpdateException;

  /**
   * get all types
   *
   * @return a list of tape names
   * @throws SchemaUpdateException
   */
  List<String> getFieldTypes() throws SchemaUpdateException;

  /**
   * get all copy field rules
   *
   * @return a list of all copy field rules
   * @throws SchemaUpdateException
   */
  List<CopyField> getCopyFields() throws SchemaUpdateException;

  /**
   * get all dynamic fields
   *
   * @return a list of dynamic field's names
   * @throws SchemaUpdateException
   */
  List<String> getDynamicFields() throws SchemaUpdateException;

  /**
   * add new fields
   *
   * @param fields the {@link Field}s to add
   * @throws SchemaUpdateException
   */
  void addFields(final List<Field> fields) throws SchemaUpdateException;

  /**
   * add new copy field rules
   *
   * @param copyFields the {@link CopyField}s to add
   * @throws SchemaUpdateException
   */
  void addCopyFields(final List<CopyField> copyFields) throws SchemaUpdateException;

  /**
   * add new field types
   *
   * @param fields the {@link FieldType}s to add
   * @throws SchemaUpdateException
   */
  void addFieldTypes(final List<FieldType> fields) throws SchemaUpdateException;

  /**
   * delete fields
   *
   * @param fields the field's names to delete
   * @throws SchemaUpdateException
   */
  void deleteFields(final List<String> fields) throws SchemaUpdateException;

  /**
   * delete copy field rules
   *
   * @param copyFields the {@link CopyField}s to delete
   * @throws SchemaUpdateException
   */
  void deleteCopyFields(final List<CopyField> copyFields) throws SchemaUpdateException;

  /**
   * delete field types
   *
   * @param fieldTypes the field types' names to delete
   * @throws SchemaUpdateException
   */
  void deleteFieldTypes(final List<String> fieldTypes) throws SchemaUpdateException;

  /**
   * delete dynamic fields
   *
   * @param dynamicFields the dynamic fields' names to delete
   * @throws SchemaUpdateException
   */
  void deleteDynamicFields(final List<String> dynamicFields) throws SchemaUpdateException;
}
