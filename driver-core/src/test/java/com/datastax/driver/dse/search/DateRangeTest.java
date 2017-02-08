/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.search;


import com.datastax.driver.core.LocalDate;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.Date;

import static com.datastax.driver.core.ParseUtils.parseDate;
import static com.datastax.driver.dse.search.DateRange.DateRangeBound.Precision.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class DateRangeTest {

    @Test(groups = "unit", dataProvider = "parseAndFormat")
    public void should_parse_and_format(String source) throws ParseException {
        DateRange parsed = DateRange.parse(source);
        assertThat(parsed.toString()).isEqualTo(source);
    }

    @Test(groups = "unit")
    public void should_use_proleptic_parser() throws ParseException {
        DateRange parsed = DateRange.parse("[0000 TO 0000-01-02]");
        assertThat(parsed.getLowerBound().getTimestamp().getTime())
                // Use LocalDate as reference since it is also proleptic
                .isEqualTo(LocalDate.fromYearMonthDay(0, 1, 1).getMillisSinceEpoch());
    }

    @DataProvider
    private Object[][] parseAndFormat() {
        return new Object[][]{
                {"[2011-01 TO 2015]"},
                {"[2010-01-02 TO 2015-05-05T13]"},
                {"[1973-06-30T13:57:28.123Z TO 1999-05-05T14:14:59]"},
                // leap year
                {"[2010-01-01T15 TO 2016-02]"},
                // pre-epoch
                {"[1500 TO 1501]"},
                {"[0001 TO 0001-01-02]"},
                {"[0000 TO 0000-01-02]"},
                {"[-0001 TO -0001-01-02]"},
                // unbounded
                {"[* TO 2014-12-01]"},
                {"[1999 TO *]"},
                {"[* TO *]"},
                // single bound ranges
                // AD/BC era boundary
                {"0001-01-01"},
                {"-0001-01-01"},
                {"-0009"},
                {"2000-11"},
                {"*"}
        };
    }

    @Test(groups = "unit")
    public void should_not_parse_and_format_invalid_strings() {
        parseAndCheckException("foo", 0);
        parseAndCheckException("[foo TO *]", 1);
        parseAndCheckException("[* TO foo]", 6);
    }

    private void parseAndCheckException(String source, int index) {
        try {
            DateRange.parse(source);
            fail("Expected DateTimeParseException");
        } catch (ParseException e) {
            assertThat(e.getMessage()).contains(source);
            assertThat(e.getErrorOffset()).isEqualTo(index);
        }
    }

    @Test(groups = "unit")
    public void should_not_parse_inverted_range() {
        try {
            DateRange.parse("[2001-01 TO 2000]");
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo(
                    "Lower bound of a date range should be before upper bound, got: [2001-01 TO 2000]");
        }
    }

    @Test(groups = "unit")
    public void should_round_up() throws ParseException {
        Date timestamp = parseDate("2011-02-03T04:05:16.789Z");
        assertThat(MILLISECOND.roundUp(timestamp)).isEqualTo(parseDate("2011-02-03T04:05:16.789Z"));
        assertThat(SECOND.roundUp(timestamp)).isEqualTo(parseDate("2011-02-03T04:05:16.999Z"));
        assertThat(MINUTE.roundUp(timestamp)).isEqualTo(parseDate("2011-02-03T04:05:59.999Z"));
        assertThat(HOUR.roundUp(timestamp)).isEqualTo(parseDate("2011-02-03T04:59:59.999Z"));
        assertThat(DAY.roundUp(timestamp)).isEqualTo(parseDate("2011-02-03T23:59:59.999Z"));
        assertThat(MONTH.roundUp(timestamp)).isEqualTo(parseDate("2011-02-28T23:59:59.999Z"));
        assertThat(YEAR.roundUp(timestamp)).isEqualTo(parseDate("2011-12-31T23:59:59.999Z"));
    }

    @Test(groups = "unit")
    public void should_round_down() throws ParseException {
        Date timestamp = parseDate("2011-02-03T04:05:16.789Z");
        assertThat(MILLISECOND.roundDown(timestamp)).isEqualTo(parseDate("2011-02-03T04:05:16.789Z"));
        assertThat(SECOND.roundDown(timestamp)).isEqualTo(parseDate("2011-02-03T04:05:16.000Z"));
        assertThat(MINUTE.roundDown(timestamp)).isEqualTo(parseDate("2011-02-03T04:05:00.000Z"));
        assertThat(HOUR.roundDown(timestamp)).isEqualTo(parseDate("2011-02-03T04:00:00.000Z"));
        assertThat(DAY.roundDown(timestamp)).isEqualTo(parseDate("2011-02-03T00:00:00.000Z"));
        assertThat(MONTH.roundDown(timestamp)).isEqualTo(parseDate("2011-02-01T00:00:00.000Z"));
        assertThat(YEAR.roundDown(timestamp)).isEqualTo(parseDate("2011-01-01T00:00:00.000Z"));
    }

    @Test(groups = "unit")
    public void should_not_equate_single_date_open_to_both_open_range() throws ParseException {
        assertThat(DateRange.parse("*")).isNotEqualTo(DateRange.parse("[* TO *]"));
    }

    @Test(groups = "unit")
    public void should_not_equate_same_ranges_with_different_precisions() throws ParseException {
        assertThat(DateRange.parse("[2001 TO 2002]")).isNotEqualTo(DateRange.parse("[2001-01 TO 2002-12]"));
    }

    @Test(groups = "unit")
    public void should_give_same_hashcode_to_equal_objects() throws ParseException {
        assertThat(DateRange.parse("[2001 TO 2002]").hashCode()).isEqualTo(DateRange.parse("[2001 TO 2002]").hashCode());
    }

}
