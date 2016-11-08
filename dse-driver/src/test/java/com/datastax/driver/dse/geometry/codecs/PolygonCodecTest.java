/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.geometry.codecs;

import com.datastax.driver.dse.geometry.Polygon;
import org.testng.annotations.DataProvider;

import static com.datastax.driver.dse.geometry.Utils.p;

public class PolygonCodecTest extends GeometryCodecTest<Polygon, PolygonCodec> {

    private Polygon polygon = new Polygon(p(30, 10), p(10, 20), p(20, 40), p(40, 40));

    public PolygonCodecTest() {
        super(PolygonCodec.INSTANCE);
    }

    @Override
    @DataProvider
    public Object[][] serde() {
        return new Object[][]{
                {null, null},
                {polygon, polygon}
        };
    }

    @Override
    @DataProvider
    public Object[][] format() {
        return new Object[][]{
                {null, "NULL"},
                {polygon, "'POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))'"}
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
                {"'POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))'", polygon},
                {" ' Polygon ( ( 30 10, 40 40, 20 40, 10 20, 30 10 ) ) ' ", polygon}
        };
    }

}
