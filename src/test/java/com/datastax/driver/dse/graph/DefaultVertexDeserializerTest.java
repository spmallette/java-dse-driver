/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.google.common.io.Resources;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import static com.datastax.driver.dse.graph.GraphAssertions.assertThat;
import static com.google.common.base.Charsets.UTF_8;

public class DefaultVertexDeserializerTest {

    @Test(groups = "unit")
    public void should_deserialize_as_vertex() throws Exception {
        Vertex vertex = GraphJsonUtils.INSTANCE.readStringAsTree(Resources.toString(getClass().getResource("/vertex1.json"), UTF_8)).as(Vertex.class);
        assertVertex(vertex);
    }

    private void assertVertex(Vertex vertex) throws IOException {

        assertThat(vertex)
                .isInstanceOf(DefaultVertex.class)
                .hasLabel("god")
                .hasProperty("name", "neptune")
                .hasProperty("nicknames", "Neppy")
                .hasProperty("nicknames", "Flipper")
                .hasProperty("age", 4500);

        assertThat(vertex.getId().asMap())
                .containsEntry("member_id", 0)
                .containsEntry("community_id", 950424)
                .containsEntry("~label", "god")
                .containsEntry("group_id", 3);

        Iterator<VertexProperty> nicknames = vertex.getProperties("nicknames");

        VertexProperty neppy = nicknames.next();
        assertThat(neppy)
                .hasKey("nicknames")
                .hasParent(vertex)
                .hasValue("Neppy")
                .hasProperty("time", new Date(22), Date.class);

        VertexProperty flipper = nicknames.next();
        assertThat(flipper)
                .hasKey("nicknames")
                .hasParent(vertex)
                .hasValue("Flipper")
                .hasProperty("time", new Date(25), Date.class);


    }
}
