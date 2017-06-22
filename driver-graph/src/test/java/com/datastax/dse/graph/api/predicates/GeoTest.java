/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph.api.predicates;

import com.datastax.driver.dse.geometry.Point;
import com.datastax.driver.dse.geometry.Polygon;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GeoTest {

    @Test(groups = "unit")
    public void testToDegrees() {
        assertThat(Geo.Unit.DEGREES.toDegrees(100.0)).isEqualTo(100.0);
        assertThat(Geo.Unit.MILES.toDegrees(68.9722)).isEqualTo(0.9982455747535043);
        assertThat(Geo.Unit.KILOMETERS.toDegrees(111.0)).isEqualTo(0.9982456082154465);
        assertThat(Geo.Unit.METERS.toDegrees(111000.0)).isEqualTo(0.9982456082154464);
    }


    @Test(groups = "unit")
    public void testCartesianPredicate() {
        P<Object> inside = Geo.inside(new Point(30, 30), 14.142135623730951);
        assertThat(inside.test(new Point(40, 40))).isTrue();
        assertThat(inside.test(new Point(40.1, 40))).isFalse();
    }


    @Test(groups = "unit")
    public void testGeoPredicate() {
        P<Object> inside = Geo.inside(new Point(30, 30), 12.908258700131379, Geo.Unit.DEGREES);
        assertThat(inside.test(new Point(40, 40))).isTrue();
        assertThat(inside.test(new Point(40.1, 40))).isFalse();
    }

    @Test(groups = "unit")
    public void testPolygonSearch() {
        P<Object> inside = Geo.inside(new Polygon(new Point(30, 30), new Point(40, 40), new Point(40, 30)));
        assertThat(inside.test(new Point(35, 32))).isTrue();
        assertThat(inside.test(new Point(33, 37))).isFalse();
    }
}
