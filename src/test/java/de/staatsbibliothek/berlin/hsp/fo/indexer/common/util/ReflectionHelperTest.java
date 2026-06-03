package de.staatsbibliothek.berlin.hsp.fo.indexer.common.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;

class ReflectionHelperTest {
  @Test
  void whenGetMethodByNameIsCalled_thenMethodIsReturned() {
    Optional<Method> method = ReflectionHelper.getMethodByName(ReflectionTestModel.class, "m1");

    assertThat(method, isPresent());
    assertThat(method.get()
        .getName(), is("m1"));
  }

  @Test
  void whenGetMethodByNameIsCalledWithNotExistingName_thenEmptyOptionalIsReturned() {
    Optional<Method> method = ReflectionHelper.getMethodByName(ReflectionTestModel.class, "m5");

    assertThat(method, isEmpty());
  }

  @Test
  void whenCreateInstanceIsCalledWithValidClassName_thenAnInstanceIsReturned() {
    Optional<Object> object = ReflectionHelper.createInstance(ReflectionTestModel.class.getName());

    assertThat(object, isPresent());
    assertThat(object, is(notNullValue()));
    assertThat(object.get(), instanceOf(ReflectionTestModel.class));
  }

  @Test
  void whenCreateInstanceIsCalledWithInValidClassName_thenAnEmptyOptionalIsReturned() {
    Optional<Object> object = ReflectionHelper.createInstance("invalidClassName");

    assertThat(object, isEmpty());
  }

  @Test
  void whenCreateInstancesIsCalled_OnlyInstancesOfValidClassNamesAreReturned() {
    Map<String, Object> instanceMap = ReflectionHelper.createInstances(List.of(ReflectionTestModel.class.getName()));

    /* duplicates are removes, therefore check for one entry only */
    assertThat(instanceMap, aMapWithSize(1));
    assertThat(instanceMap, is(notNullValue()));
    assertThat(instanceMap.get(ReflectionTestModel.class.getName()), instanceOf(ReflectionTestModel.class));
  }
}
