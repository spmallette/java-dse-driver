/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.geometry;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.esri.core.geometry.ogc.OGCLineString;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.datastax.driver.dse.geometry.Utils.p;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class LineStringTest {

    private final LineString lineString = new LineString(p(30, 10), p(10, 30), p(40, 40));

    private final String wkt = "LINESTRING (30 10, 10 30, 40 40)";

    private final String json = "{\"type\":\"LineString\",\"coordinates\":[[30.0,10.0],[10.0,30.0],[40.0,40.0]]}";

    @Test(groups = "unit")
    public void should_parse_valid_well_known_text() {
        assertThat(LineString.fromWellKnownText(wkt)).isEqualTo(lineString);
    }

    @Test(groups = "unit")
    public void should_fail_to_parse_invalid_well_known_text() {
        assertInvalidWkt("linestring()");
        assertInvalidWkt("linestring(30 10 20, 10 30 20)"); // 3d
        assertInvalidWkt("linestring(0 0, 1 1, 0 1, 1 0)"); // crossing itself
        assertInvalidWkt("superlinestring(30 10, 10 30, 40 40)");
    }

    @Test(groups = "unit")
    public void should_convert_to_well_known_text() {
        assertThat(lineString.toString()).isEqualTo(wkt);
    }

    @Test(groups = "unit")
    public void should_convert_to_well_know_binary() {
        ByteBuffer actual = lineString.asWellKnownBinary();

        ByteBuffer expected = ByteBuffer.allocate(1024).order(ByteOrder.nativeOrder());
        expected.position(0);
        expected.put((byte) (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? 1 : 0)); // endianness
        expected.putInt(2);  // type
        expected.putInt(3);  // num lineStrings
        expected.putDouble(30);  // x1
        expected.putDouble(10);  // y1
        expected.putDouble(10);  // x2
        expected.putDouble(30);  // y2
        expected.putDouble(40);  // x3
        expected.putDouble(40);  // y3
        expected.flip();

        assertThat(actual).isEqualTo(expected);
    }

    @Test(groups = "unit")
    public void should_load_from_well_know_binary() {
        ByteBuffer bb = ByteBuffer.allocate(1024).order(ByteOrder.nativeOrder());
        bb.position(0);
        bb.put((byte) (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? 1 : 0)); // endianness
        bb.putInt(2);  // type
        bb.putInt(3);  // num lineStrings
        bb.putDouble(30);  // x1
        bb.putDouble(10);  // y1
        bb.putDouble(10);  // x2
        bb.putDouble(30);  // y2
        bb.putDouble(40);  // x3
        bb.putDouble(40);  // y3
        bb.flip();

        assertThat(LineString.fromWellKnownBinary(bb)).isEqualTo(lineString);
    }

    @Test(groups = "unit")
    public void should_parse_valid_geo_json() {
        assertThat(LineString.fromGeoJson(json)).isEqualTo(lineString);
    }

    @Test(groups = "unit")
    public void should_convert_to_geo_json() {
        assertThat(lineString.asGeoJson()).isEqualTo(json);
    }

    @Test(groups = "unit")
    public void should_convert_to_ogc_line_string() {
        assertThat(lineString.getOgcGeometry()).isInstanceOf(OGCLineString.class);
    }

    @Test(groups = "unit")
    public void should_produce_same_hashcode_for_equal_objects() {
        LineString line1 = new LineString(p(30, 10), p(10, 30), p(40, 40));
        LineString line2 = LineString.fromWellKnownText(wkt);
        assertThat(line1).isEqualTo(line2);
        assertThat(line1.hashCode()).isEqualTo(line2.hashCode());
    }

    @Test(groups = "unit")
    public void should_expose_points() {
        assertThat(lineString.getPoints())
                .containsOnly(p(30, 10), p(10, 30), p(40, 40));
        assertThat(LineString.fromWellKnownText(wkt).getPoints())
                .containsOnly(p(30, 10), p(10, 30), p(40, 40));
    }

    @Test(groups = "unit")
    public void should_serialize_and_deserialize() throws Exception {
        assertThat(Utils.serializeAndDeserialize(lineString)).isEqualTo(lineString);
    }

    @Test(groups = "unit")
    public void should_contain_self() {
        assertThat(lineString.contains(lineString)).isTrue();
    }

    @Test(groups = "unit")
    public void should_contain_all_intersected_points_except_start_and_end() {
        LineString s = new LineString(p(0, 0), p(0, 30), p(30, 30));
        assertThat(s.contains(p(0, 0))).isFalse();
        assertThat(s.contains(p(0, 15))).isTrue();
        assertThat(s.contains(p(0, 30))).isTrue();
        assertThat(s.contains(p(15, 30))).isTrue();
        assertThat(s.contains(p(30, 30))).isFalse();
    }

    @Test(groups = "unit")
    public void should_contain_substring() {
        assertThat(lineString.contains(new LineString(p(30, 10), p(10, 30)))).isTrue();
    }

    @Test(groups = "unit")
    public void should_not_contain_unrelated_string() {
        assertThat(lineString.contains(new LineString(p(10, 10), p(30, 30)))).isFalse();
    }

    @Test(groups = "unit")
    public void should_not_contain_polygon() {
        LineString s = new LineString(p(0, 0), p(0, 30), p(30, 30), p(30, 0));
        Polygon p = new Polygon(p(10, 10), p(10, 20), p(20, 20), p(20, 10));
        assertThat(s.contains(p)).isFalse();
    }

    @Test(groups = "unit")
    public void should_accept_empty_shape() throws Exception {
        LineString s = LineString.fromWellKnownText("LINESTRING EMPTY");
        assertThat(s.getOgcGeometry().isEmpty()).isTrue();
    }

    private void assertInvalidWkt(String s) {
        try {
            LineString.fromWellKnownText(s);
            fail("Should have thrown InvalidTypeException");
        } catch (InvalidTypeException e) {
            // expected
        }
    }

}
