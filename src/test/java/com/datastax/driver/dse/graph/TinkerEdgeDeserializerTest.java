/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.google.common.io.Resources;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Charsets.UTF_8;


@SuppressWarnings("unchecked")
public class TinkerEdgeDeserializerTest {

    @Test(groups = "unit")
    public void should_deserialize_as_tinker_edge() throws Exception {
        Edge edge = GraphJsonUtils.INSTANCE.readStringAsTree(Resources.toString(getClass().getResource("/edge1.json"), UTF_8)).as(Edge.class);
        assertTinkerEdge(edge);
    }

    private void assertTinkerEdge(Edge edge) throws IOException {
        TinkerGraphAssertions.assertThat(edge)
                .hasLabel("battled")
                .hasInVLabel("monster")
                .hasOutVLabel("demigod")
                // TinkerProperty<Date> cannot work, only JSON natural types
                // can be used (string, boolean, integer etc.)
                .hasProperty("time", "1970-01-01T00:00:00.022Z")
                .hasProperty("place", "POINT (39 22)");

        Map<String, Object> id = (Map<String, Object>) edge.id();
        Assertions.assertThat(id)
                .containsKeys("out_vertex", "local_id", "in_vertex");

        Map<String, Object> inVertex = (Map<String, Object>) id.get("in_vertex");
        Assertions.assertThat(inVertex)
                .containsEntry("member_id", 0)
                .containsEntry("community_id", 587008)
                .containsEntry("~label", "monster")
                .containsEntry("group_id", 5);

        Map<String, Object> outVertex = (Map<String, Object>) id.get("out_vertex");
        Assertions.assertThat(outVertex)
                .containsEntry("member_id", 0)
                .containsEntry("community_id", 587008)
                .containsEntry("~label", "demigod")
                .containsEntry("group_id", 9);

        Assertions.assertThat(((Map<String, Object>) edge.inVertex().id()))
                .containsEntry("member_id", 0)
                .containsEntry("community_id", 587008)
                .containsEntry("~label", "monster")
                .containsEntry("group_id", 5);

        Assertions.assertThat(((Map<String, Object>) edge.outVertex().id()))
                .containsEntry("member_id", 0)
                .containsEntry("community_id", 587008)
                .containsEntry("~label", "demigod")
                .containsEntry("group_id", 9);

    }
}
