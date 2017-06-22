/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.geometry;


import com.datastax.driver.core.exceptions.InvalidTypeException;
import org.testng.annotations.Test;

import static com.datastax.driver.dse.geometry.Utils.p;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class DistanceTest {

    private Point point = new Point(1.1, 2.2);
    private Distance distance = new Distance(point, 7.0);

    // spec that driver generates.
    private final String wkt = "DISTANCE((1.1 2.2) 7.0)";


    @Test(groups = "unit")
    public void should_parse_valid_well_known_text() {
        Distance fromWkt = Distance.fromWellKnownText(wkt);
        assertThat(fromWkt.getRadius()).isEqualTo(7.0);
        assertThat(fromWkt.getCenter()).isEqualTo(point);
        assertThat(Distance.fromWellKnownText(wkt)).isEqualTo(distance);
        // whitespace doesn't matter between distance and spec.
        assertThat(Distance.fromWellKnownText("DISTANCE ((1.1 2.2) 7.0)")).isEqualTo(distance);
        // case doesn't matter.
        assertThat(Distance.fromWellKnownText("distance((1.1 2.2) 7.0)")).isEqualTo(distance);
    }

    @Test(groups = "unit")
    public void should_fail_to_parse_invalid_well_known_text() {
        assertInvalidWkt("dist((1.1 2.2) 3.3)");
    }

    @Test(groups = "unit")
    public void should_convert_to_well_known_text() {
        assertThat(distance.toString()).isEqualTo(wkt);
    }

    @Test(groups = "unit")
    public void should_contain_point() {
        assertThat(distance.contains(new Point(2.0, 3.0))).isTrue();
    }

    @Test(groups = "unit")
    public void should_not_contain_point() {
        // y axis falls outside of distance
        assertThat(distance.contains(new Point(2.0, 9.3))).isFalse();
    }

    @Test(groups = "unit")
    public void should_contain_linestring() {
        assertThat(distance.contains(new LineString(new Point(2.0, 3.0), new Point(3.1, 6.2), new Point(-1.0, -2.0)))).isTrue();
    }

    @Test(groups = "unit")
    public void should_not_contain_linestring() {
        // second point falls outside of distance at y axis.
        assertThat(distance.contains(new LineString(new Point(2.0, 3.0), new Point(3.1, 9.2), new Point(-1.0, -2.0)))).isFalse();
    }

    @Test(groups = "unit")
    public void should_contain_polygon() {
        Polygon polygon = new Polygon(p(3, 1), p(1, 2), p(2, 4), p(4, 4));
        assertThat(distance.contains(polygon)).isTrue();
    }

    @Test(groups = "unit")
    public void should_not_contain_polygon() {
        Polygon polygon = new Polygon(p(3, 1), p(1, 2), p(2, 4), p(10, 4));
        // final point falls outside of distance at x axis.
        assertThat(distance.contains(polygon)).isFalse();
    }

    @Test(groups = "unit", expectedExceptions = UnsupportedOperationException.class)
    public void getOgcGeometry_throws_UOE() {
        distance.getOgcGeometry();
    }

    @Test(groups = "unit", expectedExceptions = UnsupportedOperationException.class)
    public void asWellKnownBinary_throws_UOE() {
        distance.asWellKnownBinary();
    }

    @Test(groups = "unit", expectedExceptions = UnsupportedOperationException.class)
    public void asGeoJson_throws_UOE() {
        distance.asGeoJson();
    }

    private void assertInvalidWkt(String s) {
        try {
            Distance.fromWellKnownText(s);
            fail("Should have thrown InvalidTypeException");
        } catch (InvalidTypeException e) {
            // expected
        }
    }

}
