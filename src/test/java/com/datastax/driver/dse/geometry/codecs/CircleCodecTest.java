/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.geometry.codecs;

import com.datastax.driver.dse.geometry.Circle;
import org.testng.annotations.DataProvider;

public class CircleCodecTest extends GeometryCodecTest<Circle, CircleCodec> {

    public CircleCodecTest() {
        super(CircleCodec.INSTANCE);
    }

    @Override
    @DataProvider
    public Object[][] serde() {
        return new Object[][]{
                {null, null},
                {new Circle(1, 2, 3), new Circle(1, 2, 3)},
                {new Circle(-1.1, -2.2, 3.3), new Circle(-1.1, -2.2, 3.3)}
        };
    }

    @Override
    @DataProvider
    public Object[][] format() {
        return new Object[][]{
                {null, "NULL"},
                {new Circle(1, 2, 3), "'CIRCLE ((1 2) 3)'"},
                {new Circle(-1.1, -2.2, 3.3), "'CIRCLE ((-1.1 -2.2) 3.3)'"}
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
                {"'CIRCLE ((1.0 2.0) 3.0)'", new Circle(1, 2, 3)},
                {"' circle ( (-1.1 -2.2)  3.3 ) '", new Circle(-1.1, -2.2, 3.3)}
        };
    }

}
