/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.geometry.codecs;

import com.datastax.driver.dse.geometry.LineString;
import org.testng.annotations.DataProvider;

import static com.datastax.driver.dse.geometry.Utils.p;


public class LineStringCodecTest extends GeometryCodecTest<LineString, LineStringCodec> {

    private LineString lineString = new LineString(p(30, 10), p(10, 30), p(40, 40));

    public LineStringCodecTest() {
        super(LineStringCodec.INSTANCE);
    }

    @Override
    @DataProvider
    public Object[][] serde() {
        return new Object[][]{
                {null, null},
                {lineString, lineString}
        };
    }

    @Override
    @DataProvider
    public Object[][] format() {
        return new Object[][]{
                {null, "NULL"},
                {lineString, "'LINESTRING (30 10, 10 30, 40 40)'"}
        };
    }

    @Override
    @DataProvider
    public Object[][] parse() {
        return new Object[][]{
                {null, null},
                {"", null},
                {" ", null},
                {"NULL", null},
                {" NULL ", null},
                {"'LINESTRING (30 10, 10 30, 40 40)'", lineString},
                {" ' LineString (30 10, 10 30, 40 40 ) ' ", lineString}
        };
    }

}
