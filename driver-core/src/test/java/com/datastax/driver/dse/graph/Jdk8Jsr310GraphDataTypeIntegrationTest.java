/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.IgnoreJDK6Requirement;
import org.testng.annotations.DataProvider;

@IgnoreJDK6Requirement
@SuppressWarnings("Since15")
@DseVersion(major = 5.0)
public class Jdk8Jsr310GraphDataTypeIntegrationTest extends GraphDataTypeIntegrationTest {

    @DataProvider
    public static Object[][] dataTypeSamples() {
        return new Object[][]{
                // JDK 8 types
                {"Duration()", java.time.Duration.parse("P2DT3H4M")},
                {"Timestamp()", java.time.Instant.parse("2016-05-12T16:12:23.999Z")},
        };
    }

    @DataProvider
    public static Object[][] dataTypeSamples51() {
        return new Object[][]{
                {"Date()", java.time.LocalDate.of(2016, 5, 12)},
                {"Date()", "1999-07-29"},
                {"Time()", java.time.LocalTime.of(18, 30, 41, 554000000)},
                // 18:30:41.554010034
                {"Time()", java.time.LocalTime.ofNanoOfDay(66641554010034L)}
        };
    }

}
