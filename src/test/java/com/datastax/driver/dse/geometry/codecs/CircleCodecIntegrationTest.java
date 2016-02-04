/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.geometry.codecs;

import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.geometry.Circle;
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
