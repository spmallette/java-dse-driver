/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.geometry.codecs;

import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.geometry.Point;
import org.testng.collections.Lists;

import static com.datastax.driver.dse.geometry.Utils.p;

@DseVersion(major = 5.0)
public class PointCodecIntegrationTest extends GeometryCodecIntegrationTest<Point> {
    public PointCodecIntegrationTest() {
        super("PointType", Lists.newArrayList(p(-1.0, -5), p(0, 0), p(1.1, 2.2),
                p(Double.MIN_VALUE, 0), p(Double.MAX_VALUE, Double.MIN_VALUE)));
    }
}
