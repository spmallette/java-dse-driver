/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.dse.geometry.*;
import com.google.common.net.InetAddresses;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.UUID;

import static com.datastax.driver.dse.geometry.Utils.p;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that all types supported by DSE Graph can be serialized and deserialized
 * with GraphJsonUtils.
 */
@SuppressWarnings("Since15")
public class GraphJsonUtilsTest {

    UUID uuid = UUID.randomUUID();

    InetAddress inet4 = InetAddresses.forString("127.0.0.1");

    InetAddress inet6 = InetAddresses.forString("2001:db8:85a3:0:0:8a2e:370:7334");

    Point point = new Point(-1, 2);

    LineString lineString = new LineString(new Point(-1, 2), new Point(-2, 3));

    Polygon polygon = Polygon.builder()
            .addRing(p(35, 10), p(45, 45), p(15, 40), p(10, 20), p(35, 10))
            .addRing(p(20, 30), p(35, 35), p(30, 20), p(20, 30))
            .build();

    Distance distance = new Distance(point, 32.0);

    Date date = new Date(1458143616);

    byte[] helloWorld = new byte[]{72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100, 33};

    ByteBuffer helloWorldBuf = ByteBuffer.wrap(helloWorld);

    @Test(groups = "unit")
    public void should_serialize_supported_types_graphson_1_0() throws Exception {

        assertThat(GraphJsonUtils.writeValueAsString(true)).isEqualTo("true");
        assertThat(GraphJsonUtils.writeValueAsString((byte) -123)).isEqualTo("-123");
        assertThat(GraphJsonUtils.writeValueAsString((short) -1234)).isEqualTo("-1234");
        assertThat(GraphJsonUtils.writeValueAsString(-1234)).isEqualTo("-1234");
        assertThat(GraphJsonUtils.writeValueAsString(-1234L)).isEqualTo("-1234");
        assertThat(GraphJsonUtils.writeValueAsString(new BigInteger("-1234"))).isEqualTo("-1234");
        assertThat(GraphJsonUtils.writeValueAsString(-1234.56d)).isEqualTo("-1234.56");
        assertThat(GraphJsonUtils.writeValueAsString(-1234.56f)).isEqualTo("-1234.56");
        assertThat(GraphJsonUtils.writeValueAsString(new BigDecimal("-1234.56"))).isEqualTo("-1234.56");
        assertThat(GraphJsonUtils.writeValueAsString("foo")).isEqualTo("\"foo\"");

        assertThat(GraphJsonUtils.writeValueAsString(date)).isEqualTo("1458143616");
        assertThat(GraphJsonUtils.writeValueAsString(uuid)).isEqualTo("\"" + uuid.toString() + "\"");
        assertThat(GraphJsonUtils.writeValueAsString(inet4)).isEqualTo("\"127.0.0.1\"");
        assertThat(GraphJsonUtils.writeValueAsString(inet6)).isEqualTo("\"2001:db8:85a3:0:0:8a2e:370:7334\"");

        assertThat(GraphJsonUtils.writeValueAsString(point)).isEqualTo("\"" + point.asWellKnownText() + "\"");
        assertThat(GraphJsonUtils.writeValueAsString(lineString)).isEqualTo("\"" + lineString.asWellKnownText() + "\"");
        assertThat(GraphJsonUtils.writeValueAsString(polygon)).isEqualTo("\"" + polygon.asWellKnownText() + "\"");

    }

    @Test(groups = "unit")
    public void should_deserialize_supported_types_graphson_1_0() throws Exception {

        assertThat(GraphJsonUtils.readStringAsTree("true").asBoolean()).isEqualTo(true);
        assertThat(GraphJsonUtils.readStringAsTree("-123").as(Byte.class)).isEqualTo((byte) -123);
        assertThat(GraphJsonUtils.readStringAsTree("-1234").as(Short.class)).isEqualTo((short) -1234);
        assertThat(GraphJsonUtils.readStringAsTree("-1234").asInt()).isEqualTo(-1234);
        assertThat(GraphJsonUtils.readStringAsTree("-1234").asLong()).isEqualTo(-1234L);
        assertThat(GraphJsonUtils.readStringAsTree("-1234.56").asDouble()).isEqualTo(-1234.56d);
        assertThat(GraphJsonUtils.readStringAsTree("-1234.56").as(Float.class)).isEqualTo(-1234.56f);
        assertThat(GraphJsonUtils.readStringAsTree("\"foo\"").asString()).isEqualTo("foo");

        assertThat(GraphJsonUtils.readStringAsTree("-1234").as(BigInteger.class)).isEqualTo(new BigInteger("-1234"));
        assertThat(GraphJsonUtils.readStringAsTree("-1234.56").as(BigDecimal.class)).isEqualTo(new BigDecimal("-1234.56"));

        assertThat(GraphJsonUtils.readStringAsTree("1458143616").as(Date.class)).isEqualTo(date);
        assertThat(GraphJsonUtils.readStringAsTree("\"" + uuid.toString() + "\"").as(UUID.class)).isEqualTo(uuid);

        assertThat(GraphJsonUtils.readStringAsTree("\"127.0.0.1\"").as(InetAddress.class)).isEqualTo(inet4);
        assertThat(GraphJsonUtils.readStringAsTree("\"2001:db8:85a3:0:0:8a2e:370:7334\"").as(InetAddress.class)).isEqualTo(inet6);
        assertThat(GraphJsonUtils.readStringAsTree("\"127.0.0.1\"").as(Inet4Address.class)).isEqualTo(inet4);
        assertThat(GraphJsonUtils.readStringAsTree("\"2001:db8:85a3:0:0:8a2e:370:7334\"").as(Inet6Address.class)).isEqualTo(inet6);

        assertThat(GraphJsonUtils.readStringAsTree("\"" + point.asWellKnownText() + "\"").as(Geometry.class)).isEqualTo(point);
        assertThat(GraphJsonUtils.readStringAsTree("\"" + lineString.asWellKnownText() + "\"").as(Geometry.class)).isEqualTo(lineString);
        assertThat(GraphJsonUtils.readStringAsTree("\"" + polygon.asWellKnownText() + "\"").as(Geometry.class)).isEqualTo(polygon);
        assertThat(GraphJsonUtils.readStringAsTree("\"" + point.asWellKnownText() + "\"").as(Point.class)).isEqualTo(point);
        assertThat(GraphJsonUtils.readStringAsTree("\"" + lineString.asWellKnownText() + "\"").as(LineString.class)).isEqualTo(lineString);
        assertThat(GraphJsonUtils.readStringAsTree("\"" + polygon.asWellKnownText() + "\"").as(Polygon.class)).isEqualTo(polygon);

    }

    @Test(groups = "unit")
    public void should_deserialize_supported_types_graphson_2_0() throws Exception {
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("true").asBoolean()).isEqualTo(true);
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("\"foo\"").asString()).isEqualTo("foo");

        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"g:Int32\", \"@value\": -1234}").asInt()).isEqualTo(-1234);
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"g:Int64\", \"@value\": -1234}").asLong()).isEqualTo(-1234L);
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"g:Double\", \"@value\": -1234.56}").asDouble()).isEqualTo(-1234.56d);
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"g:Float\", \"@value\": -1234.56}").as(Float.class)).isEqualTo(-1234.56f);
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"g:UUID\", \"@value\": \"" + uuid.toString() + "\"}").as(UUID.class)).isEqualTo(uuid);
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"g:Class\", \"@value\": \"java.util.UUID\"}").as(Class.class)).isEqualTo(UUID.class);
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"g:Date\", \"@value\": 1458143616}").as(Date.class)).isEqualTo(date);

        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"gx:Char\", \"@value\": 72}").as(Character.class)).isEqualTo('H');
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"gx:Byte\", \"@value\": -123}").as(Byte.class)).isEqualTo((byte) -123);
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"gx:Int16\", \"@value\": -1234}").as(Short.class)).isEqualTo((short) -1234);
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"gx:InetAddress\", \"@value\": \"127.0.0.1\"}").as(InetAddress.class)).isEqualTo(inet4);
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"gx:InetAddress\", \"@value\": \"2001:db8:85a3:0:0:8a2e:370:7334\"}").as(InetAddress.class)).isEqualTo(inet6);
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"gx:BigInteger\", \"@value\": -1234}").as(BigInteger.class)).isEqualTo(new BigInteger("-1234"));
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"gx:BigDecimal\", \"@value\": -1234.56}").as(BigDecimal.class)).isEqualTo(new BigDecimal("-1234.56"));
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"gx:ByteBuffer\",\"@value\":\"SGVsbG8gV29ybGQh\"}").as(ByteBuffer.class)).isEqualTo(helloWorldBuf);

        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"dse:Point\", \"@value\": \"" + point.asWellKnownText() + "\"}").as(Geometry.class)).isEqualTo(point);
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"dse:LineString\", \"@value\": \"" + lineString.asWellKnownText() + "\"}").as(Geometry.class)).isEqualTo(lineString);
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"dse:Polygon\", \"@value\": \"" + polygon.asWellKnownText() + "\"}").as(Geometry.class)).isEqualTo(polygon);
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"dse:Distance\", \"@value\": \"" + distance.asWellKnownText() + "\"}").as(Geometry.class)).isEqualTo(distance);
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"dse:Point\", \"@value\": \"" + point.asWellKnownText() + "\"}").as(Point.class)).isEqualTo(point);
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"dse:LineString\", \"@value\": \"" + lineString.asWellKnownText() + "\"}").as(LineString.class)).isEqualTo(lineString);
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"dse:Polygon\", \"@value\": \"" + polygon.asWellKnownText() + "\"}").as(Polygon.class)).isEqualTo(polygon);
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"dse:Distance\", \"@value\": \"" + distance.asWellKnownText() + "\"}").as(Distance.class)).isEqualTo(distance);
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\": \"dse:Blob\", \"@value\": \"SGVsbG8gV29ybGQh\"}").as(byte[].class)).isEqualTo(helloWorld);
    }

}
