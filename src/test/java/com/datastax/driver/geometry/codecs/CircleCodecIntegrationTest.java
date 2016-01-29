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

import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.geometry.Circle;
import org.testng.collections.Lists;

@DseVersion(major = 5.0)
public class CircleCodecIntegrationTest extends GeometryCodecIntegrationTest<Circle> {
    public CircleCodecIntegrationTest() {
        super("CircleType", Lists.newArrayList(
                new Circle(1, 2, 3),
                new Circle(-1.1, -2.2, 3.3),
                new Circle(10, 7, 3.1),
                // Creates a big number for the center, TODO: Check to see if this is valid.
                new Circle(Double.MAX_VALUE, Double.MIN_VALUE + 20, 10))
        );
    }
}
