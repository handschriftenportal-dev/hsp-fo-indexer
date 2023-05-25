package de.staatsbibliothek.berlin.hsp.fo.indexer.common.util;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.util.TypeHelper;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

class TypeHelperTest {
    @Test
    @DisplayName("String to Boolean")
    void testCastToType_BooleanValue() {
        final Object res = TypeHelper.castToType("false", Boolean.class.getSimpleName(), "testField");

        assertThat(res, instanceOf(Boolean.class));
        assertThat((Boolean) res, equalTo(Boolean.FALSE));
    }

    @Test
    @DisplayName("String to Float")
    void testCastToType_FloatValue() {
        final Object res = TypeHelper.castToType("1.2345", Float.class.getSimpleName(), "testField");

        assertThat(res, instanceOf(Float.class));
        assertThat((Float) res, equalTo(1.2345f));
    }

    @Test
    @DisplayName("String to Integer")
    void testCastToType_IntegerValue() {
        final Object res = TypeHelper.castToType("12345", Integer.class.getSimpleName(), "testField");

        assertThat(res, instanceOf(Integer.class));
        assertThat((Integer) res, equalTo(12345));
    }

    @Test
    @DisplayName("String to String")
    void testCastToType_StringValue() {
        final Object res = TypeHelper.castToType("12345", String.class.getSimpleName(), "testField");

        assertThat(res, instanceOf(String.class));
        assertThat((String) res, equalTo("12345"));
    }

    @Test
    @DisplayName("String to Date")
    void testCastToType_DateValue() {
        final Object res = TypeHelper.castToType("2020-10-07", Date.class.getSimpleName(), "testField");
        final Calendar cal = Calendar.getInstance();
        cal.setTime((Date) res);

        assertThat(res, instanceOf(Date.class));

        assertThat(cal.get(Calendar.YEAR), equalTo(2020));
        assertThat(cal.get(Calendar.MONTH), equalTo(9));
        assertThat(cal.get(Calendar.DAY_OF_MONTH), equalTo(7));
    }

    @Test
    @DisplayName("String array to Integer array")
    void testCastToArray_Integer() {
        final List<String> values = Lists.newArrayList("1", "2", "3.0");
        final Object[] res = TypeHelper.castToArray(values, Integer.class, "testField");

        assertThat(res, instanceOf(Integer[].class));
        assertThat(res.length, equalTo(2));
        assertThat(res[0], equalTo(1));
        assertThat(res[1], equalTo(2));
    }

    @Test
    @DisplayName("String array to Float array")
    void testCastToArray_Float() {
        final List<String> values = Lists.newArrayList("1", "2", "3.0");
        final Object[] res = TypeHelper.castToArray(values, Float.class, "testField");

        assertThat(res, instanceOf(Float[].class));
        assertThat(res.length, equalTo(3));
        assertThat(res[0], equalTo(1f));
        assertThat(res[1], equalTo(2f));
        assertThat(res[2], equalTo(3f));
    }

    @Test
    @DisplayName("String array to Boolean array")
    void testCastToArray_Boolean() {
        final List<String> values = Lists.newArrayList("true", "false", "fluffy");
        final Object[] res = TypeHelper.castToArray(values, Boolean.class, "testField");

        assertThat(res, instanceOf(Boolean[].class));
        assertThat(res.length, equalTo(3));
        assertThat(res[0], equalTo(Boolean.TRUE));
        assertThat(res[1], equalTo(Boolean.FALSE));
        assertThat(res[2], equalTo(Boolean.FALSE));
    }

    @Test
    @DisplayName("String array to Date array")
    void testCastToArray_Date() {
        final List<String> values = Lists.newArrayList("0005-01-01", "1970-01-01", "2020-10-18");
        final Object[] res = TypeHelper.castToArray(values, Date.class, "testField");
        final Calendar cal = Calendar.getInstance();

        assertThat(res, instanceOf(Date[].class));
        assertThat(res.length, equalTo(3));

        cal.setTime((Date) res[0]);
        assertThat(cal.get(Calendar.YEAR), equalTo(5));
        assertThat(cal.get(Calendar.MONTH), equalTo(0));
        assertThat(cal.get(Calendar.DAY_OF_MONTH), equalTo(1));

        cal.setTime((Date) res[1]);
        assertThat(cal.get(Calendar.YEAR), equalTo(1970));
        assertThat(cal.get(Calendar.MONTH), equalTo(0));
        assertThat(cal.get(Calendar.DAY_OF_MONTH), equalTo(1));

        cal.setTime((Date) res[2]);
        assertThat(cal.get(Calendar.YEAR), equalTo(2020));
        assertThat(cal.get(Calendar.MONTH), equalTo(9));
        assertThat(cal.get(Calendar.DAY_OF_MONTH), equalTo(18));
    }
}
