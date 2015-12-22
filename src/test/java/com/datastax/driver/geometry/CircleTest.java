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
package com.datastax.driver.geometry;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.datastax.driver.core.utils.Bytes;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class CircleTest {

    private Circle circle1 = new Circle(1, 2, 3);
    private String json1 = "{\"type\":\"Circle\", \"coordinates\":[1.0, 2.0], \"radius\": 3.0}";

    private Circle circle2 = new Circle(1.2, 2.3, 3.4);
    private String json2 = "{\"type\":\"Circle\", \"coordinates\":[1.2, 2.3], \"radius\": 3.4}";

    @Test(groups = "unit")
    public void should_parse_valid_well_known_text() {
        assertThat(Circle.fromWellKnownText("circle((1 2) 3)")).isEqualTo(circle1);
        assertThat(Circle.fromWellKnownText("circle ( ( 1 2 ) 3 )")).isEqualTo(circle1);
        assertThat(Circle.fromWellKnownText("   circle    (    (    1    2    )   3    )   ")).isEqualTo(circle1);
        assertThat(Circle.fromWellKnownText("CIRCLE((1 2) 3)")).isEqualTo(circle1);
        assertThat(Circle.fromWellKnownText("CiRcLe((1 2) 3)")).isEqualTo(circle1);
        assertThat(Circle.fromWellKnownText("circle((1.2 2.3) 3.4)")).isEqualTo(circle2);
        assertThat(Circle.fromWellKnownText("circle((-1.2 -2.3) 3.4)")).isEqualTo(new Circle(-1.2, -2.3, 3.4));
    }

    @Test(groups = "unit")
    public void should_fail_to_parse_invalid_well_known_text() {
        assertInvalidWkt("circ le((1 2) 3)");
        assertInvalidWkt("circle(1 2) 3)");
        assertInvalidWkt("circle((1 2) 3");
        assertInvalidWkt("circle((1 2 3)");
        assertInvalidWkt("circle((1, 2), 3)");
        assertInvalidWkt("circle(1, 2, 3)");
        assertInvalidWkt("circ((1 2) 3)");
        assertInvalidWkt("circle((one two) four)");
        assertInvalidWkt("circle((1) four)");
        assertInvalidWkt("circle((1- 2-) 4)");
    }

    @Test(groups = "unit")
    public void should_convert_to_well_known_text() {
        assertThat(circle1.toString()).isEqualTo("CIRCLE ((1 2) 3)");
        assertThat(circle2.toString()).isEqualTo("CIRCLE ((1.2 2.3) 3.4)");
    }

    @Test(groups = "unit")
    public void should_convert_to_well_know_binary_with_native_endian() {
        ByteBuffer expected = ByteBuffer.allocate(1024).order(ByteOrder.nativeOrder());
        expected.position(0);
        expected.put((byte) (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? 1 : 0)); // endianness
        expected.putInt(101);
        expected.putDouble(1);
        expected.putDouble(2);
        expected.putDouble(3);
        expected.flip();
        ByteBuffer actual = circle1.asWellKnownBinary();
        assertThat(actual).isEqualTo(expected);
    }

    @Test(groups = "unit")
    public void should_load_from_well_know_binary_with_native_endian() {
        ByteOrder order = ByteOrder.nativeOrder();
        ByteBuffer bb = ByteBuffer.allocate(29).order(order);
        bb.position(0);
        bb.put((byte) (order == ByteOrder.LITTLE_ENDIAN ? 1 : 0)); // endianness
        bb.putInt(101);
        bb.putDouble(1);
        bb.putDouble(2);
        bb.putDouble(3);
        bb.flip();
        assertThat(Circle.fromWellKnownBinary(bb)).isEqualTo(circle1);
    }

    @Test(groups = "unit")
    public void should_convert_to_well_know_binary_with_other_endian() {
        // just to confirm that ByteBuffer respects the byte ordering
        ByteBuffer bb;
        bb = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(0xFF);
        bb.flip();
        assertThat(Bytes.toHexString(bb)).isEqualTo("0xff000000");
        bb = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        bb.putInt(0xFF);
        bb.flip();
        assertThat(Bytes.toHexString(bb)).isEqualTo("0x000000ff");
        ByteOrder order = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        bb = ByteBuffer.allocate(29).order(order);
        bb.position(0);
        bb.put((byte) (order == ByteOrder.LITTLE_ENDIAN ? 1 : 0));
        bb.putInt(101);
        bb.putDouble(1);
        bb.putDouble(2);
        bb.putDouble(3);
        bb.flip();
        assertThat(Circle.fromWellKnownBinary(bb)).isEqualTo(circle1);
    }

    @Test(groups = "unit")
    public void should_fail_to_load_from_invalid_well_known_binary() {
        // underflow
        ByteBuffer b1 = ByteBuffer.allocate(8).order(ByteOrder.nativeOrder());
        b1.put((byte) (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? 1 : 0));
        b1.putInt(101).flip();
        assertInvalidWkb(b1);
        // bad type
        ByteBuffer b2 = ByteBuffer.allocate(1024).order(ByteOrder.nativeOrder());
        b2.position(0);
        b2.put((byte) (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? 1 : 0));
        b2.putInt(102).putDouble(0).putDouble(1).putDouble(2).flip();
        assertInvalidWkb(b2);
    }

    @Test(groups = "unit")
    public void should_convert_to_geo_json() {
        assertThat(circle1.asGeoJson().replaceAll("\\s+", "")).isEqualTo(json1.replaceAll("\\s+", ""));
        assertThat(circle2.asGeoJson().replaceAll("\\s+", "")).isEqualTo(json2.replaceAll("\\s+", ""));
    }

    @Test(groups = "unit")
    public void should_parse_valid_geo_json() {
        assertThat(Circle.fromGeoJson(json1)).isEqualTo(circle1);
        assertThat(Circle.fromGeoJson(json2)).isEqualTo(circle2);
    }

    @Test(groups = "unit")
    public void should_fail_to_parse_invalid_geo_json() {
        // missing fields
        assertInvalidJson("{\"coordinates\":[1, 2], \"radius\": 3}");
        assertInvalidJson("{\"type\":\"Circle\",\"radius\": 3}");
        assertInvalidJson("{\"type\":\"Circle\",\"coordinates\":[1, 2]}");
        // extra field
        assertInvalidJson("{\"type\":\"Circle\",\"coordinates\":[a, 2], \"radius\": 3, dimension: \"x\"}");
        // misspelled field
        assertInvalidJson("{\"type\":\"Circle\",\"coordinates\":[1, 2], \"radis\": 3}");
        // bad field type
        assertInvalidJson("{\"type\":\"Circle\",\"coordinates\":1, \"radius\": 3}");
        assertInvalidJson("{\"type\":\"NotACircle\",\"coordinates\":[1, 2],\"radius\": 3}");
        // wrong coordinates
        assertInvalidJson("{\"type\":\"Circle\",\"coordinates\":[1, 2, 3], \"radius\": 3}");
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void should_fail_if_radius_is_zero() {
        new Circle(1, 2, 0);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void should_fail_if_radius_is_negative() {
        new Circle(1, 2, -3);
    }

    @Test(groups = "unit")
    public void should_produce_same_hashcode_for_equal_objects() {
        Circle circle1 = new Circle(10, 20, 30);
        Circle circle2 = new Circle(new Point(10, 20), 30);
        assertThat(circle1).isEqualTo(circle2);
        assertThat(circle1.hashCode()).isEqualTo(circle2.hashCode());
    }

    @Test(groups = "unit")
    public void should_serialize_and_deserialize() throws Exception {
        assertThat(Utils.serializeAndDeserialize(circle1)).isEqualTo(circle1);
    }

    private void assertInvalidWkt(String s) {
        try {
            Circle.fromWellKnownText(s);
            fail("Should have thrown InvalidTypeException");
        } catch (InvalidTypeException e) {
            // expected
        }
    }

    private void assertInvalidWkb(ByteBuffer bb) {
        try {
            Circle.fromWellKnownBinary(bb);
            fail("Should have thrown InvalidTypeException");
        } catch (InvalidTypeException e) {
            // expected
        }
    }

    private void assertInvalidJson(String s) {
        try {
            Circle.fromGeoJson(s);
            fail("Should have thrown InvalidTypeException");
        } catch (InvalidTypeException e) {
            // expected
        }
    }

}
