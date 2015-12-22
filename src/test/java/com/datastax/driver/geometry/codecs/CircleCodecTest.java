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

import com.datastax.driver.geometry.Circle;
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
