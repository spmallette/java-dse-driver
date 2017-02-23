/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.extras.codecs.jdk8;

import com.datastax.driver.core.Assertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalTime;

import static com.datastax.driver.core.ParseUtils.quote;
import static com.datastax.driver.core.ProtocolVersion.V4;
import static org.assertj.core.api.Assertions.assertThat;

public class LocalTimeCodecTest {

    @DataProvider(name = "LocalTimeCodecTest.parse")
    public Object[][] parseParameters() {
        LocalTime time = LocalTime.parse("13:25:47.123456789");
        String nanosOfDay = quote(Long.toString(time.toNanoOfDay()));
        return new Object[][]{
                {null, null},
                {"", null},
                {"NULL", null},
                {nanosOfDay, LocalTime.parse("13:25:47.123456789")},
                {"'13:25:47'", LocalTime.parse("13:25:47")},
                {"'13:25:47.123'", LocalTime.parse("13:25:47.123")},
                {"'13:25:47.123456'", LocalTime.parse("13:25:47.123456")},
                {"'13:25:47.123456789'", LocalTime.parse("13:25:47.123456789")}
        };
    }

    @DataProvider(name = "LocalTimeCodecTest.format")
    public Object[][] formatParameters() {
        return new Object[][]{
                {null, "NULL"},
                {LocalTime.NOON, "'12:00:00.000'"},
                {LocalTime.parse("02:20:47.999999"), "'02:20:47.999'"}
        };
    }

    @Test(groups = "unit", dataProvider = "LocalTimeCodecTest.parse")
    public void should_parse_valid_formats(String input, LocalTime expected) {
        // when
        LocalTime actual = LocalTimeCodec.instance.parse(input);
        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test(groups = "unit", dataProvider = "LocalTimeCodecTest.format")
    public void should_serialize_and_format_valid_object(LocalTime input, String expected) {
        // when
        String actual = LocalTimeCodec.instance.format(input);
        // then
        Assertions.assertThat(LocalTimeCodec.instance).withProtocolVersion(V4).canSerialize(input);
        assertThat(actual).isEqualTo(expected);
    }

}
