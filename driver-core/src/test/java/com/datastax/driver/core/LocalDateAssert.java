/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalDateAssert extends AbstractAssert<LocalDateAssert, LocalDate> {
    public LocalDateAssert(LocalDate actual) {
        super(actual, LocalDateAssert.class);
    }

    public LocalDateAssert hasDaysSinceEpoch(int expected) {
        assertThat(actual.getDaysSinceEpoch()).isEqualTo(expected);
        return this;
    }

    public LocalDateAssert hasMillisSinceEpoch(long expected) {
        assertThat(actual.getMillisSinceEpoch()).isEqualTo(expected);
        return this;
    }

    public LocalDateAssert hasYearMonthDay(int expectedYear, int expectedMonth, int expectedDay) {
        assertThat(actual.getYear()).isEqualTo(expectedYear);
        assertThat(actual.getMonth()).isEqualTo(expectedMonth);
        assertThat(actual.getDay()).isEqualTo(expectedDay);
        return this;
    }

    public LocalDateAssert hasToString(String expected) {
        assertThat(actual.toString()).isEqualTo(expected);
        return this;
    }
}
