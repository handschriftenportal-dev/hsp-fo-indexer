package de.staatsbibliothek.berlin.hsp.fo.indexer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.CopyField;
import de.staatsbibliothek.berlin.hsp.fo.indexer.model.schema.Field;
import de.staatsbibliothek.berlin.hsp.fo.indexer.service.impl.SchemaServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.EmbeddedSolrServer;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.annotation.EmbeddedSolrInstance;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.extension.HspSolrSetupExtension;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.util.FileHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.matcher.SolrFieldTypeMatcher.canBeCopiedInto;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

@ExtendWith(HspSolrSetupExtension.class)
@TestMethodOrder(MethodOrderer.Random.class)
class SchemaServiceTest {

  private static SchemaServiceImpl schemaService;

  @EmbeddedSolrInstance
  private EmbeddedSolrServer server;

  @BeforeEach
  public void init() {
    schemaService = new SchemaServiceImpl(server.getSolrClient("hsp"));
  }

  @Test
  void testGetFields() throws Exception {
    final List<String> fields = schemaService.getFields();
    assertThat(fields, hasSize(5));
  }

  @Test
  void testGetTypes() throws Exception {
    final List<String> types = schemaService.getFieldTypes();
    assertThat(types, hasSize(19));
  }

  @Test
  void testGetCopyFields() throws Exception {
    final List<CopyField> copyFields = schemaService.getCopyFields();
    assertThat(copyFields, hasSize(0));
  }

  @Test
  void getDynamicFields() throws Exception {
    final List<String> dynFields = schemaService.getDynamicFields();
    assertThat(dynFields, hasSize(1));
  }

  @Test
  void testAddField() throws Exception {
    List<String> fields = schemaService.getFields();
    assertThat(fields, hasSize(5));

    final List<Field> newFields = new ArrayList<>() {
      {
        add(new Field("test", "string"));
      }
    };
    schemaService.addFields(newFields);

    fields = schemaService.getFields();
    assertThat(fields, hasSize(6));

    /* clean up */
    schemaService.deleteFields(newFields.stream()
        .map(Field::getName)
        .collect(Collectors.toList()));
  }

  @Test
  void testAddCopyField() throws Exception {
    final List<Field> newFields = new ArrayList<>() {
      {
        add(new Field("test", "string"));
      }
    };

    final List<CopyField> newCopyFields = new ArrayList<>() {
      {
        add(CopyField.builder()
            .source("id")
            .dest("test")
            .build());
      }
    };

    schemaService.addFields(newFields);
    schemaService.addCopyFields(newCopyFields);

    List<CopyField> copyFields = schemaService.getCopyFields();
    assertThat(copyFields, hasSize(1));

    /* clean up */
    schemaService.deleteCopyFields(newCopyFields);
    schemaService.deleteFields(newFields.stream()
        .map(Field::getName)
        .collect(Collectors.toList()));
  }

  @Test
  void testCopyFieldRules() throws Exception {
    // get fields from fields.json
    final List<Field> mappedFields = FileHelper.fromLocation(new TypeReference<>() {
    }, "schema/fields.json");

    // get copy field rules from copy-fields.json
    final List<CopyField> mappedCopyFieldRules = FileHelper.fromLocation(new TypeReference<>() {
    }, "schema/copy-fields.json");
    final List<CopyField> filteredCopyFieldRules = mappedCopyFieldRules.stream()
        .filter(cpf -> !cpf.getSource()
            .equals("id"))
        .collect(Collectors.toList());

    for (CopyField cp : filteredCopyFieldRules) {
      // ignore wild carded fields
      if (cp.getSource()
          .contains("*")) {
        continue;
      }
      // get field definition from source field name
      Optional<Field> srcField = mappedFields.stream()
          .filter(f -> f.getName()
              .equals(cp.getSource()))
          .findFirst();
      // get field definition from target field name
      Optional<Field> destField = mappedFields.stream()
          .filter(f -> f.getName()
              .equals(cp.getDest()))
          .findFirst();
      assertThat(srcField, isPresent());
      assertThat(destField, isPresent());
      assertThat(String.format("multiValue check failed for %s", srcField.get()
          .getName()), srcField.get()
          .isMultiValue(), is(destField.get()
          .isMultiValue()));
      assertThat(String.format("%s and %s are not compatible", srcField.get()
          .getName(), destField.get()
          .getName()), srcField.get()
          .getType(), is(canBeCopiedInto(destField.get()
          .getType())));
    }

    List<String> textMteFieldNames = mappedFields.stream()
        .filter(f -> f.getType()
            .equals("text_mte"))
        .map(f -> f.getName()
            .substring(0, f.getName()
                .lastIndexOf("-")))
        .collect(Collectors.toList());
    List<String> textFuzzyFieldNames = mappedFields.stream()
        .filter(f -> f.getType()
            .equals("text_fuzzy"))
        .map(Field::getName)
        .collect(Collectors.toList());
    assertThat(textMteFieldNames, containsInAnyOrder(textFuzzyFieldNames.toArray()));
    assertThat(textFuzzyFieldNames, containsInAnyOrder(textMteFieldNames.toArray()));
  }
}
