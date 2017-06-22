/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.geometry.codecs;

import com.datastax.driver.dse.geometry.Point;
import org.testng.annotations.DataProvider;

public class PointCodecTest extends GeometryCodecTest<Point, PointCodec> {

    public PointCodecTest() {
        super(PointCodec.INSTANCE);
    }

    @Override
    @DataProvider
    public Object[][] serde() {
        return new Object[][]{
                {null, null},
                {new Point(1, 2), new Point(1, 2)},
                {new Point(-1.1, -2.2), new Point(-1.1, -2.2)}
        };
    }

    @Override
    @DataProvider
    public Object[][] format() {
        return new Object[][]{
                {null, "NULL"},
                {new Point(1, 2), "'POINT (1 2)'"},
                {new Point(-1.1, -2.2), "'POINT (-1.1 -2.2)'"}
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
                {"'POINT ( 1 2 )'", new Point(1, 2)},
                {"'POINT ( 1.0 2.0 )'", new Point(1, 2)},
                {"' point ( -1.1 -2.2 )'", new Point(-1.1, -2.2)},
                {" ' Point ( -1.1 -2.2 ) ' ", new Point(-1.1, -2.2)}
        };
    }

}
