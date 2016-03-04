/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.geometry;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.esri.core.geometry.ogc.OGCPolygon;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.datastax.driver.dse.geometry.Utils.p;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class PolygonTest {

    private Polygon polygon = new Polygon(p(30, 10), p(10, 20), p(20, 40), p(40, 40));

    private String wkt = "POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))";

    private String json = "{\"type\":\"Polygon\",\"coordinates\":[[[30.0,10.0],[10.0,20.0],[20.0,40.0],[40.0,40.0],[30.0,10.0]]]}";


    @Test(groups = "unit")
    public void should_parse_valid_well_known_text() {
        assertThat(Polygon.fromWellKnownText(wkt)).isEqualTo(polygon);
    }

    @Test(groups = "unit")
    public void should_fail_to_parse_invalid_well_known_text() {
        assertInvalidWkt("polygon(())"); // malformed
        assertInvalidWkt("polygon((30 10 1, 40 40 1, 20 40 1, 10 20 1, 30 10 1))"); // 3d
        assertInvalidWkt("polygon((0 0, 1 1, 0 1, 1 0, 0 0))"); // crosses itself
        assertInvalidWkt("polygon123((30 10, 40 40, 20 40, 10 20, 30 10))"); // malformed
    }

    @Test(groups = "unit")
    public void should_convert_to_well_know_binary() {
        ByteBuffer actual = polygon.asWellKnownBinary();

        ByteBuffer expected = ByteBuffer.allocate(1024).order(ByteOrder.nativeOrder());
        expected.position(0);
        expected.put((byte) (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? 1 : 0)); // endianness
        expected.putInt(3);  // type
        expected.putInt(1);  // num rings
        expected.putInt(5);  // num polygons (ring 1/1)
        expected.putDouble(30);  // x1
        expected.putDouble(10);  // y1
        expected.putDouble(40);  // x2
        expected.putDouble(40);  // y2
        expected.putDouble(20);  // x3
        expected.putDouble(40);  // y3
        expected.putDouble(10);  // x4
        expected.putDouble(20);  // y4
        expected.putDouble(30);  // x5
        expected.putDouble(10);  // y5
        expected.flip();

        assertThat(actual).isEqualTo(expected);
    }

    @Test(groups = "unit")
    public void should_load_from_well_know_binary() {
        ByteBuffer bb = ByteBuffer.allocate(1024).order(ByteOrder.nativeOrder());
        bb.position(0);
        bb.put((byte) (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? 1 : 0)); // endianness
        bb.putInt(3);  // type
        bb.putInt(1);  // num rings
        bb.putInt(5);  // num polygons (ring 1/1)
        bb.putDouble(30);  // x1
        bb.putDouble(10);  // y1
        bb.putDouble(40);  // x2
        bb.putDouble(40);  // y2
        bb.putDouble(20);  // x3
        bb.putDouble(40);  // y3
        bb.putDouble(10);  // x4
        bb.putDouble(20);  // y4
        bb.putDouble(30);  // x5
        bb.putDouble(10);  // y5
        bb.flip();

        assertThat(Polygon.fromWellKnownBinary(bb)).isEqualTo(polygon);
    }

    @Test(groups = "unit")
    public void should_parse_valid_geo_json() {
        assertThat(Polygon.fromGeoJson(json)).isEqualTo(polygon);
    }

    @Test(groups = "unit")
    public void should_convert_to_geo_json() {
        assertThat(polygon.asGeoJson()).isEqualTo(json);
    }

    @Test(groups = "unit")
    public void should_convert_to_ogc_polygon() {
        assertThat(polygon.getOgcGeometry()).isInstanceOf(OGCPolygon.class);
    }

    @Test(groups = "unit")
    public void should_produce_same_hashcode_for_equal_objects() {
        Polygon polygon1 = new Polygon(p(30, 10), p(10, 20), p(20, 40), p(40, 40));
        Polygon polygon2 = Polygon.fromWellKnownText(wkt);
        assertThat(polygon1).isEqualTo(polygon2);
        assertThat(polygon1.hashCode()).isEqualTo(polygon2.hashCode());
    }

    @Test(groups = "unit")
    public void should_build_with_constructor_without_checking_orientation() {
        // By default, OGC requires outer rings to be clockwise and inner rings to be counterclockwise.
        // We disable that in our constructors.
        // This polygon has a single outer ring that is counterclockwise.
        Polygon polygon = new Polygon(p(5, 0), p(5, 3), p(0, 3), p(0, 0));
        assertThat(polygon.asWellKnownText()).isEqualTo("POLYGON ((0 0, 5 0, 5 3, 0 3, 0 0))");
    }

    @Test(groups = "unit")
    public void should_build_complex_polygon_with_builder() {
        Polygon polygon = Polygon.builder()
                .addRing(p(0, 0), p(0, 3), p(5, 3), p(5, 0))
                .addRing(p(1, 1), p(1, 2), p(2, 2), p(2, 1))
                .addRing(p(3, 1), p(3, 2), p(4, 2), p(4, 1))
                .build();
        assertThat(polygon.asWellKnownText())
                .isEqualTo("POLYGON ((0 0, 5 0, 5 3, 0 3, 0 0), (1 1, 1 2, 2 2, 2 1, 1 1), (3 1, 3 2, 4 2, 4 1, 3 1))");
    }

    @Test(groups = "unit")
    public void should_expose_rings() {
        assertThat(polygon.getExteriorRing()).containsOnly(p(30, 10), p(10, 20), p(20, 40), p(40, 40));
        assertThat(polygon.getInteriorRings().isEmpty());

        Polygon fromWkt = Polygon.fromWellKnownText(wkt);
        assertThat(fromWkt.getExteriorRing()).containsOnly(p(30, 10), p(10, 20), p(20, 40), p(40, 40));
        assertThat(fromWkt.getInteriorRings().isEmpty());

        Polygon complex = Polygon.builder()
                .addRing(p(0, 0), p(0, 3), p(5, 3), p(5, 0))
                .addRing(p(1, 1), p(1, 2), p(2, 2), p(2, 1))
                .addRing(p(3, 1), p(3, 2), p(4, 2), p(4, 1))
                .build();
        assertThat(complex.getExteriorRing()).containsOnly(p(0, 0), p(0, 3), p(5, 3), p(5, 0));
        assertThat(complex.getInteriorRings()).hasSize(2);
        assertThat(complex.getInteriorRings().get(0)).containsOnly(p(1, 1), p(1, 2), p(2, 2), p(2, 1));
        assertThat(complex.getInteriorRings().get(1)).containsOnly(p(3, 1), p(3, 2), p(4, 2), p(4, 1));

        Polygon complexFromWkt = Polygon.fromWellKnownText("POLYGON ((0 0, 5 0, 5 3, 0 3, 0 0), (1 1, 1 2, 2 2, 2 1, 1 1), (3 1, 3 2, 4 2, 4 1, 3 1))");
        assertThat(complexFromWkt.getExteriorRing()).containsOnly(p(0, 0), p(0, 3), p(5, 3), p(5, 0));
        assertThat(complexFromWkt.getInteriorRings()).hasSize(2);
        assertThat(complexFromWkt.getInteriorRings().get(0)).containsOnly(p(1, 1), p(1, 2), p(2, 2), p(2, 1));
        assertThat(complexFromWkt.getInteriorRings().get(1)).containsOnly(p(3, 1), p(3, 2), p(4, 2), p(4, 1));
    }

    @Test(groups = "unit")
    public void should_serialize_and_deserialize() throws Exception {
        assertThat(Utils.serializeAndDeserialize(polygon)).isEqualTo(polygon);
    }

    @Test(groups = "unit")
    public void should_contain_self() {
        assertThat(polygon.contains(polygon)).isTrue();
    }

    @Test(groups = "unit")
    public void should_not_contain_point_or_linestring_on_exterior_ring() {
        assertThat(polygon.contains(p(30, 10))).isFalse();
        assertThat(polygon.contains(p(30, 40))).isFalse();
        assertThat(polygon.contains(new LineString(p(35, 40), p(25, 40)))).isFalse();
    }

    @Test(groups = "unit")
    public void should_contain_interior_shape() {
        assertThat(polygon.contains(p(20, 20))).isTrue();
        assertThat(polygon.contains(new LineString(p(20, 20), p(30, 20)))).isTrue();
        assertThat(polygon.contains(new Polygon(p(20, 20), p(30, 20), p(20, 30)))).isTrue();
    }

    @Test(groups = "unit")
    public void should_not_contain_exterior_shape() {
        assertThat(polygon.contains(p(10, 10))).isFalse();
        assertThat(polygon.contains(new LineString(p(10, 10), p(20, 20)))).isFalse();
        assertThat(polygon.contains(new Polygon(p(0, 0), p(0, 10), p(10, 10)))).isFalse();
    }

    @Test(groups = "unit")
    public void should_not_contain_shapes_in_interior_hole() {
        Polygon complex = Polygon.builder()
                .addRing(p(0, 0), p(30, 0), p(30, 30), p(0, 30))
                .addRing(p(10, 10), p(20, 10), p(20, 20), p(10, 20))
                .build();
        assertThat(complex.contains(p(15, 15))).isFalse();
    }

    private void assertInvalidWkt(String s) {
        try {
            Polygon.fromWellKnownText(s);
            fail("Should have thrown InvalidTypeException");
        } catch (InvalidTypeException e) {
            // expected
        }
    }

}
