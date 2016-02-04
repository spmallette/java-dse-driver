/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.geometry.codecs;

import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.geometry.LineString;
import org.testng.collections.Lists;

import static com.datastax.driver.dse.geometry.Utils.p;

@DseVersion(major = 5.0)
public class LineStringCodecIntegrationTest extends GeometryCodecIntegrationTest<LineString> {
    public LineStringCodecIntegrationTest() {
        super("LineStringType", Lists.newArrayList(
                new LineString(p(0, 10), p(10, 0)),
                new LineString(p(30, 10), p(10, 30), p(40, 40)),
                new LineString(p(-5, 0), p(0, 10), p(10, 5)))
        );
    }
}
