/*
 *      Copyright (C) 2012-2015 DataStax Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
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
