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
package com.datastax.driver.geometry.codecs;

import com.datastax.driver.geometry.Point;
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
