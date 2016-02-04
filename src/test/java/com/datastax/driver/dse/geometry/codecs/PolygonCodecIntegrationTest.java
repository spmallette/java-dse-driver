/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.geometry.codecs;

import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.geometry.Polygon;
import org.testng.collections.Lists;

import static com.datastax.driver.dse.geometry.Utils.p;

@DseVersion(major = 5.0)
public class PolygonCodecIntegrationTest extends GeometryCodecIntegrationTest<Polygon> {

    static Polygon squareInMinDomain = new Polygon(p(Double.MIN_VALUE, Double.MIN_VALUE), p(Double.MIN_VALUE, Double.MIN_VALUE + 1),
            p(Double.MIN_VALUE + 1, Double.MIN_VALUE + 1), p(Double.MIN_VALUE + 1, Double.MIN_VALUE));


    static Polygon triangle = new Polygon(p(-5, 10), p(5, 5), p(10, -5));

    static Polygon complexPolygon = Polygon.builder()
            .addRing(p(0, 0), p(0, 3), p(5, 3), p(5, 0))
            .addRing(p(1, 1), p(1, 2), p(2, 2), p(2, 1))
            .addRing(p(3, 1), p(3, 2), p(4, 2), p(4, 1))
            .build();

    public PolygonCodecIntegrationTest() {
        super("PolygonType", Lists.newArrayList(squareInMinDomain, complexPolygon, triangle));
    }

}