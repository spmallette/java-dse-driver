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
import com.datastax.driver.geometry.Point;
import org.testng.collections.Lists;

import static com.datastax.driver.geometry.Utils.p;

@DseVersion(major = 5.0)
public class PointCodecIntegrationTest extends GeometryCodecIntegrationTest<Point> {
    public PointCodecIntegrationTest() {
        super("PointType", Lists.newArrayList(p(-1.0, -5), p(0, 0), p(1.1, 2.2),
                p(Double.MIN_VALUE, 0), p(Double.MAX_VALUE, Double.MIN_VALUE)));
    }
}
