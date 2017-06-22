/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.geometry.codecs;

import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.geometry.Point;
import org.testng.collections.Lists;

import static com.datastax.driver.dse.geometry.Utils.p;

@DseVersion("5.0.0")
public class PointCodecIntegrationTest extends GeometryCodecIntegrationTest<Point> {
    public PointCodecIntegrationTest() {
        super("PointType", Lists.newArrayList(p(-1.0, -5), p(0, 0), p(1.1, 2.2),
                p(Double.MIN_VALUE, 0), p(Double.MAX_VALUE, Double.MIN_VALUE)));
    }
}
