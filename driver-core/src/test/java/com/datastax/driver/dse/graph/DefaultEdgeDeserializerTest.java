/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.dse.IgnoreJDK6Requirement;
import com.datastax.driver.dse.geometry.Point;
import com.google.common.io.Resources;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

import static com.datastax.driver.dse.graph.GraphAssertions.assertThat;
import static com.google.common.base.Charsets.UTF_8;

@SuppressWarnings({"unchecked", "Since15"})
@IgnoreJDK6Requirement
public class DefaultEdgeDeserializerTest {
    // Expected graphson for a traversal on the gods GraphFixture: g.E().hasLabel("battled").has("place", Point.fromWellKnownText("POINT (39 22)"))

    @Test(groups = "unit")
    public void should_deserialize_as_edge_graphson_1_0() throws Exception {
        Edge edge = GraphJsonUtils.readStringAsTree(Resources.toString(getClass().getResource("/graphson-1.0/edge1.json"), UTF_8)).as(Edge.class);
        assertEdge(edge);
    }

    @Test(groups = "unit")
    public void should_deserialize_as_edge_graphson_2_0() throws Exception {
        Edge edge = GraphJsonUtils.readStringAsTreeGraphson20(Resources.toString(getClass().getResource("/graphson-2.0/edge1.json"), UTF_8)).as(Edge.class);
        assertEdge(edge);
    }

    private void assertEdge(Edge edge) throws IOException {
        assertThat(edge)
                .isInstanceOf(DefaultEdge.class)
                .hasLabel("battled")
                .hasInVLabel("monster")
                .hasOutVLabel("demigod")
                .hasProperty("time")
                .hasProperty("place", Point.fromWellKnownText("POINT (39 22)"), Point.class);
        assertThat(edge.getId().asMap())
                .containsKeys("out_vertex", "local_id", "in_vertex");
        Map<String, Object> inVertex = (Map<String, Object>) edge.getId().asMap().get("in_vertex");
        assertThat(inVertex)
                .containsEntry("member_id", 0)
                .containsEntry("community_id", 587008)
                .containsEntry("~label", "monster");
        Map<String, Object> outVertex = (Map<String, Object>) edge.getId().asMap().get("out_vertex");
        assertThat(outVertex)
                .containsEntry("member_id", 0)
                .containsEntry("community_id", 587008)
                .containsEntry("~label", "demigod");
        assertThat(edge.getInV().asMap())
                .containsEntry("member_id", 0)
                .containsEntry("community_id", 587008)
                .containsEntry("~label", "monster");
        assertThat(edge.getOutV().asMap())
                .containsEntry("member_id", 0)
                .containsEntry("community_id", 587008)
                .containsEntry("~label", "demigod");
        Property place = edge.getProperty("place");
        assertThat(place.getValue().as(Point.class))
                .isEqualTo(Point.fromWellKnownText("POINT (39 22)"));
    }
}
