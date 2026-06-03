package de.staatsbibliothek.berlin.hsp.fo.indexer.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;

class TypeHelperTest {
    @Test
    @DisplayName("String to Boolean")
    void testCastToType_BooleanValue() {
        final Optional<?> res = TypeHelper.castToJavaType(List.of("false"), Boolean.class);

        assertThat(res, isPresent());
        assertThat(res.get(), instanceOf(Boolean.class));
        assertThat(res.get(), equalTo(Boolean.FALSE));
    }

    @Test
    @DisplayName("String to Float")
    void testCastToType_FloatValue() {
        final Optional<?> res = TypeHelper.castToJavaType(List.of("1.2345"), Float.class);

        assertThat(res, isPresent());
        assertThat(res.get(), instanceOf(Float.class));
        assertThat(res.get(), equalTo(1.2345f));
    }

    @Test
    @DisplayName("String to Integer")
    void testCastToType_IntegerValue() {
        final Optional<?> res = TypeHelper.castToJavaType(List.of("12345"), Integer.class);

        assertThat(res, isPresent());
        assertThat(res.get(), instanceOf(Integer.class));
        assertThat(res.get(), equalTo(12345));
    }

    @Test
    @DisplayName("String to String")
    void testCastToType_StringValue() {
        final Optional<?> res = TypeHelper.castToJavaType(List.of("12345"), String.class);

        assertThat(res, isPresent());
        assertThat(res.get(), instanceOf(String.class));
        assertThat(res.get(), equalTo("12345"));
    }

    @Test
    @DisplayName("String to Date")
    void testCastToType_DateValue() {
        final Optional<?> res = TypeHelper.castToJavaType(List.of("2020-10-07"), Date.class);
        final Calendar cal = Calendar.getInstance();

        assertThat(res, isPresent());
        assertThat(res.get(), instanceOf(Date.class));
        cal.setTime((Date) res.get());
        assertThat(cal.get(Calendar.YEAR), equalTo(2020));
        assertThat(cal.get(Calendar.MONTH), equalTo(9));
        assertThat(cal.get(Calendar.DAY_OF_MONTH), equalTo(7));
    }

    @Test
    @DisplayName("String array to Integer array")
    void testCastToArray_Integer() {
        final Optional<?> res = TypeHelper.castToJavaType(List.of("1", "2", "3.0"), Integer[].class);

        assertThat(res, isPresent());
        assertThat(res.get(), instanceOf(Integer[].class));
        assertThat(((Integer[]) res.get()), arrayContaining(1, 2));
    }

    @Test
    @DisplayName("String array to Float array")
    void testCastToArray_Float() {
        final Optional<?> res = TypeHelper.castToJavaType(List.of("1", "2", "3.0"), Float[].class);

        assertThat(res, isPresent());
        assertThat(res.get(), instanceOf(Float[].class));
        assertThat(((Float[]) res.get()), arrayContaining(1f, 2f, 3f));
    }

    @Test
    @DisplayName("String array to Boolean array")
    void testCastToArray_Boolean() {
        final Optional<?> res = TypeHelper.castToJavaType(List.of("true", "false", "fluffy"), Boolean[].class);

        assertThat(res, isPresent());
        assertThat(((Boolean[]) res.get()), arrayContaining(Boolean.TRUE, Boolean.FALSE, Boolean.FALSE));
    }

    @Test
    @DisplayName("String array to Date array")
    void testCastToArray_Date() {
        final Optional<?> res = TypeHelper.castToJavaType(List.of("0005-01-01", "1970-01-01", "2020-10-18"), Date[].class);

        final Calendar cal = Calendar.getInstance();

        assertThat(res, isPresent());
        assertThat(res.get(), instanceOf(Date[].class));
        assertThat(((Date[]) res.get()).length, equalTo(3));

        cal.setTime(((Date[]) res.get())[0]);
        assertThat(cal.get(Calendar.YEAR), equalTo(5));
        assertThat(cal.get(Calendar.MONTH), equalTo(0));
        assertThat(cal.get(Calendar.DAY_OF_MONTH), equalTo(1));

        cal.setTime(((Date[]) res.get())[1]);
        assertThat(cal.get(Calendar.YEAR), equalTo(1970));
        assertThat(cal.get(Calendar.MONTH), equalTo(0));
        assertThat(cal.get(Calendar.DAY_OF_MONTH), equalTo(1));

        cal.setTime(((Date[]) res.get())[2]);
        assertThat(cal.get(Calendar.YEAR), equalTo(2020));
        assertThat(cal.get(Calendar.MONTH), equalTo(9));
        assertThat(cal.get(Calendar.DAY_OF_MONTH), equalTo(18));
    }
}
