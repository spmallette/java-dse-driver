/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.dse.geometry.Geometry;
import com.datastax.driver.dse.geometry.LineString;
import com.datastax.driver.dse.geometry.Point;
import com.datastax.driver.dse.geometry.Polygon;
import com.google.common.net.InetAddresses;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
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

    Date date = new Date(1458143616);

    @Test(groups = "unit")
    public void should_serialize_supported_types() throws Exception {

        GraphJsonUtils jsonUtils = GraphJsonUtils.INSTANCE;

        assertThat(jsonUtils.writeValueAsString(true)).isEqualTo("true");
        assertThat(jsonUtils.writeValueAsString((byte) -123)).isEqualTo("-123");
        assertThat(jsonUtils.writeValueAsString((short) -1234)).isEqualTo("-1234");
        assertThat(jsonUtils.writeValueAsString(-1234)).isEqualTo("-1234");
        assertThat(jsonUtils.writeValueAsString(-1234L)).isEqualTo("-1234");
        assertThat(jsonUtils.writeValueAsString(new BigInteger("-1234"))).isEqualTo("-1234");
        assertThat(jsonUtils.writeValueAsString(-1234.56d)).isEqualTo("-1234.56");
        assertThat(jsonUtils.writeValueAsString(-1234.56f)).isEqualTo("-1234.56");
        assertThat(jsonUtils.writeValueAsString(new BigDecimal("-1234.56"))).isEqualTo("-1234.56");
        assertThat(jsonUtils.writeValueAsString("foo")).isEqualTo("\"foo\"");

        assertThat(jsonUtils.writeValueAsString(date)).isEqualTo("1458143616");
        assertThat(jsonUtils.writeValueAsString(uuid)).isEqualTo("\"" + uuid.toString() + "\"");
        assertThat(jsonUtils.writeValueAsString(inet4)).isEqualTo("\"127.0.0.1\"");
        assertThat(jsonUtils.writeValueAsString(inet6)).isEqualTo("\"2001:db8:85a3:0:0:8a2e:370:7334\"");

        assertThat(jsonUtils.writeValueAsString(point)).isEqualTo("\"" + point.asWellKnownText() + "\"");
        assertThat(jsonUtils.writeValueAsString(lineString)).isEqualTo("\"" + lineString.asWellKnownText() + "\"");
        assertThat(jsonUtils.writeValueAsString(polygon)).isEqualTo("\"" + polygon.asWellKnownText() + "\"");

    }

    @Test(groups = "unit")
    public void should_deserialize_supported_types() throws Exception {

        GraphJsonUtils jsonUtils = GraphJsonUtils.INSTANCE;

        assertThat(jsonUtils.readStringAsTree("true").asBoolean()).isEqualTo(true);
        assertThat(jsonUtils.readStringAsTree("-123").as(Byte.class)).isEqualTo((byte) -123);
        assertThat(jsonUtils.readStringAsTree("-1234").as(Short.class)).isEqualTo((short) -1234);
        assertThat(jsonUtils.readStringAsTree("-1234").asInt()).isEqualTo(-1234);
        assertThat(jsonUtils.readStringAsTree("-1234").asLong()).isEqualTo(-1234L);
        assertThat(jsonUtils.readStringAsTree("-1234.56").asDouble()).isEqualTo(-1234.56d);
        assertThat(jsonUtils.readStringAsTree("-1234.56").as(Float.class)).isEqualTo(-1234.56f);
        assertThat(jsonUtils.readStringAsTree("\"foo\"").asString()).isEqualTo("foo");

        assertThat(jsonUtils.readStringAsTree("-1234").as(BigInteger.class)).isEqualTo(new BigInteger("-1234"));
        assertThat(jsonUtils.readStringAsTree("-1234.56").as(BigDecimal.class)).isEqualTo(new BigDecimal("-1234.56"));

        assertThat(jsonUtils.readStringAsTree("1458143616").as(Date.class)).isEqualTo(date);
        assertThat(jsonUtils.readStringAsTree("\"" + uuid.toString() + "\"").as(UUID.class)).isEqualTo(uuid);

        assertThat(jsonUtils.readStringAsTree("\"127.0.0.1\"").as(InetAddress.class)).isEqualTo(inet4);
        assertThat(jsonUtils.readStringAsTree("\"2001:db8:85a3:0:0:8a2e:370:7334\"").as(InetAddress.class)).isEqualTo(inet6);
        assertThat(jsonUtils.readStringAsTree("\"127.0.0.1\"").as(Inet4Address.class)).isEqualTo(inet4);
        assertThat(jsonUtils.readStringAsTree("\"2001:db8:85a3:0:0:8a2e:370:7334\"").as(Inet6Address.class)).isEqualTo(inet6);

        assertThat(jsonUtils.readStringAsTree("\"" + point.asWellKnownText() + "\"").as(Geometry.class)).isEqualTo(point);
        assertThat(jsonUtils.readStringAsTree("\"" + lineString.asWellKnownText() + "\"").as(Geometry.class)).isEqualTo(lineString);
        assertThat(jsonUtils.readStringAsTree("\"" + polygon.asWellKnownText() + "\"").as(Geometry.class)).isEqualTo(polygon);
        assertThat(jsonUtils.readStringAsTree("\"" + point.asWellKnownText() + "\"").as(Point.class)).isEqualTo(point);
        assertThat(jsonUtils.readStringAsTree("\"" + lineString.asWellKnownText() + "\"").as(LineString.class)).isEqualTo(lineString);
        assertThat(jsonUtils.readStringAsTree("\"" + polygon.asWellKnownText() + "\"").as(Polygon.class)).isEqualTo(polygon);

    }

}
