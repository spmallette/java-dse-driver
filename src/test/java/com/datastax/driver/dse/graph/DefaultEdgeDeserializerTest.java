/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.dse.geometry.Point;
import com.google.common.io.Resources;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import static com.datastax.driver.dse.graph.GraphAssertions.assertThat;
import static com.google.common.base.Charsets.UTF_8;

@SuppressWarnings("unchecked")
public class DefaultEdgeDeserializerTest {

    @Test(groups = "unit")
    public void should_deserialize_as_edge() throws Exception {
        Edge edge = GraphJsonUtils.INSTANCE.readStringAsTree(Resources.toString(getClass().getResource("/edge1.json"), UTF_8)).as(Edge.class);
        assertEdge(edge);
    }

    private void assertEdge(Edge edge) throws IOException {
        assertThat(edge)
                .isInstanceOf(DefaultEdge.class)
                .hasLabel("battled")
                .hasInVLabel("monster")
                .hasOutVLabel("demigod")
                .hasProperty("time", new Date(22), Date.class)
                .hasProperty("time", "1970-01-01T00:00:00.022Z")
                .hasProperty("place", Point.fromWellKnownText("POINT (39 22)"), Point.class);
        assertThat(edge.getId().asMap())
                .containsKeys("out_vertex", "local_id", "in_vertex");
        Map<String, Object> inVertex = (Map<String, Object>) edge.getId().asMap().get("in_vertex");
        assertThat(inVertex)
                .containsEntry("member_id", 0)
                .containsEntry("community_id", 587008)
                .containsEntry("~label", "monster")
                .containsEntry("group_id", 5);
        Map<String, Object> outVertex = (Map<String, Object>) edge.getId().asMap().get("out_vertex");
        assertThat(outVertex)
                .containsEntry("member_id", 0)
                .containsEntry("community_id", 587008)
                .containsEntry("~label", "demigod")
                .containsEntry("group_id", 9);
        assertThat(edge.getInV().asMap())
                .containsEntry("member_id", 0)
                .containsEntry("community_id", 587008)
                .containsEntry("~label", "monster")
                .containsEntry("group_id", 5);
        assertThat(edge.getOutV().asMap())
                .containsEntry("member_id", 0)
                .containsEntry("community_id", 587008)
                .containsEntry("~label", "demigod")
                .containsEntry("group_id", 9);
        Property time = edge.getProperty("time");
        assertThat(time.getValue().as(Date.class))
                .isEqualTo(new Date(22));
        Property place = edge.getProperty("place");
        assertThat(place.getValue().as(Point.class))
                .isEqualTo(Point.fromWellKnownText("POINT (39 22)"));
    }
}
