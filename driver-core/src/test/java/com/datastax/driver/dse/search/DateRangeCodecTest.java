/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.search;


import com.datastax.driver.core.exceptions.InvalidTypeException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.text.ParseException;

import static com.datastax.driver.core.ProtocolVersion.V4;
import static org.assertj.core.api.Assertions.assertThat;

public class DateRangeCodecTest {

    @Test(groups = "unit", dataProvider = "serializeAndDeserialize")
    public void should_serialize_and_deserialize(DateRange dateRange) {
        DateRange parsed = DateRangeCodec.INSTANCE.deserialize(DateRangeCodec.INSTANCE.serialize(dateRange, V4), V4);
        assertThat(parsed).isEqualTo(dateRange);
    }

    @Test(groups = "unit")
    public void should_fail_on_unknown_date_range_type() {
        try {
            DateRangeCodec.INSTANCE.deserialize(ByteBuffer.wrap(new byte[]{127}), V4);
        } catch (InvalidTypeException e) {
            assertThat(e).hasMessage("Unknown date range type: 127");
        }
    }

    @Test(groups = "unit", dataProvider = "parseAndFormat")
    public void should_format_and_parse(String dateRange) {
        String formatted = DateRangeCodec.INSTANCE.format(DateRangeCodec.INSTANCE.parse(dateRange));
        assertThat(formatted).isEqualTo(dateRange == null ? "NULL" : dateRange);
    }

    @Test(groups = "unit")
    public void should_fail_on_unparseable_date_range() {
        try {
            DateRangeCodec.INSTANCE.parse("foo");
        } catch (InvalidTypeException e) {
            assertThat(e).hasMessage("Invalid date range literal: foo");
        }
    }

    @DataProvider
    private Object[][] serializeAndDeserialize() throws ParseException {
        return new Object[][]{
                {null},
                {DateRange.parse("[2011-01 TO 2015]")},
                {DateRange.parse("[2010-01-02 TO 2015-05-05T13]")},
                {DateRange.parse("[1973-06-30T13:57:28.123Z TO 1999-05-05T14:14:59]")},
                {DateRange.parse("[2010-01-01T15 TO 2016-02]")},
                {DateRange.parse("[1500 TO 1501]")},
                {DateRange.parse("[0001-01-01 TO 0001-01-01]")},
                {DateRange.parse("[0001-01-01 TO 0001-01-02]")},
                {DateRange.parse("[0000-01-01 TO 0000-01-01]")},
                {DateRange.parse("[0000-01-01 TO 0000-01-02]")},
                {DateRange.parse("[-0001-01-01 TO -0001-01-01]")},
                {DateRange.parse("[-0001-01-01 TO -0001-01-02]")},
                {DateRange.parse("[* TO 2014-12-01]")},
                {DateRange.parse("[1999 TO *]")},
                {DateRange.parse("[* TO *]")},
                {DateRange.parse("-0009")},
                {DateRange.parse("2000-11")},
                {DateRange.parse("*")}
        };
    }

    @DataProvider
    private Object[][] parseAndFormat() {
        return new Object[][]{
                {null},
                {"NULL"},
                {"'[2011-01 TO 2015]'"},
                {"'[2010-01-02 TO 2015-05-05T13]'"},
                {"'[1973-06-30T13:57:28.123Z TO 1999-05-05T14:14:59]'"},
                {"'[2010-01-01T15 TO 2016-02]'"},
                {"'[1500 TO 1501]'"},
                {"'[0001-01-01 TO 0001-01-01]'"},
                {"'[0001-01-01 TO 0001-01-02]'"},
                {"'[0000-01-01 TO 0000-01-01]'"},
                {"'[0000-01-01 TO 0000-01-02]'"},
                {"'[-0001-01-01 TO -0001-01-01]'"},
                {"'[-0001-01-01 TO -0001-01-02]'"},
                {"'[* TO 2014-12-01]'"},
                {"'[1999 TO *]'"},
                {"'[* TO *]'"},
                {"'-0009'"},
                {"'2000-11'"},
                {"'*'"}
        };
    }

}
