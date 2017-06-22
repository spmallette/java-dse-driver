/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.geometry.codecs;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.dse.geometry.Polygon;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.UUID;

import static com.datastax.driver.dse.geometry.Utils.p;
import static org.assertj.core.api.Assertions.assertThat;

@DseVersion("5.0.0")
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

    /**
     * Validates that an empty {@link Polygon} can be inserted and retrieved.
     *
     * @jira_ticket JAVA-1076
     * @test_category dse:graph
     */
    @Test(groups = "short")
    public void should_insert_and_retrieve_empty_polygon() {
        Polygon empty = Polygon.builder().build();
        UUID key = UUIDs.random();
        session().execute("INSERT INTO tbl (k, g) VALUES (?, ?)", key, empty);

        Row row = session().execute("SELECT g from tbl where k=?", key).one();

        assertThat(row.get("g", Polygon.class).getInteriorRings()).isEmpty();
        assertThat(row.get("g", Polygon.class).getExteriorRing()).isEmpty();
    }

}