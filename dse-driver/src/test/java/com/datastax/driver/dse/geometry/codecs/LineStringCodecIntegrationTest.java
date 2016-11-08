/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.geometry.codecs;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.dse.geometry.LineString;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.UUID;

import static com.datastax.driver.dse.geometry.Utils.p;
import static org.assertj.core.api.Assertions.assertThat;

@DseVersion(major = 5.0)
public class LineStringCodecIntegrationTest extends GeometryCodecIntegrationTest<LineString> {
    public LineStringCodecIntegrationTest() {
        super("LineStringType", Lists.newArrayList(
                new LineString(p(0, 10), p(10, 0)),
                new LineString(p(30, 10), p(10, 30), p(40, 40)),
                new LineString(p(-5, 0), p(0, 10), p(10, 5)))
        );
    }

    /**
     * Validates that an empty {@link LineString} can be inserted and retrieved.
     *
     * @jira_ticket JAVA-1076
     * @test_category dse:graph
     */
    @Test(groups = "short")
    public void should_insert_and_retrieve_empty_linestring() {
        LineString empty = LineString.fromWellKnownText("LINESTRING EMPTY");
        UUID key = UUIDs.random();
        session().execute("INSERT INTO tbl (k, g) VALUES (?, ?)", key, empty);

        Row row = session().execute("SELECT g from tbl where k=?", key).one();

        assertThat(row.get("g", LineString.class).getPoints()).isEmpty();
    }
}
