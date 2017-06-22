/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.dse.IgnoreJDK6Requirement;
import com.google.common.io.Resources;
import org.testng.annotations.Test;

import java.util.Iterator;

import static com.datastax.driver.dse.graph.GraphAssertions.assertThat;
import static com.google.common.base.Charsets.UTF_8;

@IgnoreJDK6Requirement
@SuppressWarnings("Since15")
public class DefaultVertexDeserializerTest {
    // Expected graphson for a traversal on the gods GraphFixture: g.V().hasLabel("god").has("name", "neptune")

    @Test(groups = "unit")
    public void should_deserialize_as_vertex_graphson_1_0() throws Exception {
        Vertex vertex = GraphJsonUtils.readStringAsTree(Resources.toString(getClass().getResource("/graphson-1.0/vertex1.json"), UTF_8)).as(Vertex.class);
        assertVertex(vertex);
    }

    @Test(groups = "unit")
    public void should_deserialize_as_vertex_graphson_2_0() throws Exception {
        Vertex vertex = GraphJsonUtils.readStringAsTreeGraphson20(Resources.toString(getClass().getResource("/graphson-2.0/vertex1.json"), UTF_8)).as(Vertex.class);
        assertVertex(vertex);
    }

    private void assertVertex(Vertex vertex) throws Exception {

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
                .containsEntry("~label", "god");

        Iterator<VertexProperty> nicknames = vertex.getProperties("nicknames");

        VertexProperty neppy = nicknames.next();

        assertThat(neppy)
                .hasKey("nicknames")
                .hasParent(vertex)
                .hasValue("Neppy")
                .hasProperty("time");

        VertexProperty flipper = nicknames.next();
        assertThat(flipper)
                .hasKey("nicknames")
                .hasParent(vertex)
                .hasValue("Flipper")
                .hasProperty("time");
    }
}
