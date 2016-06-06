/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.google.common.io.Resources;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static com.datastax.driver.dse.graph.TinkerGraphAssertions.assertThat;
import static com.google.common.base.Charsets.UTF_8;


@SuppressWarnings("unchecked")
public class TinkerVertexDeserializerTest {

    @Test(groups = "unit")
    public void should_deserialize_as_tinker_vertex() throws Exception {
        Vertex vertex = GraphJsonUtils.INSTANCE.readStringAsTree(Resources.toString(getClass().getResource("/vertex1.json"), UTF_8)).as(Vertex.class);
        assertTinkerVertex(vertex);
    }

    private void assertTinkerVertex(Vertex vertex) throws IOException {

        assertThat(vertex)
                .hasLabel("god")
                .hasProperty("name", "neptune")
                .hasProperty("nicknames", "Neppy")
                .hasProperty("nicknames", "Flipper")
                .hasProperty("age", 4500);

        Map<String, Object> id = (Map<String, Object>) vertex.id();
        assertThat(id)
                .containsEntry("member_id", 0)
                .containsEntry("community_id", 950424)
                .containsEntry("~label", "god")
                .containsEntry("group_id", 3);

        Iterator<VertexProperty<String>> nicknames = vertex.properties("nicknames");
        assertThat(nicknames.hasNext());

        VertexProperty<String> neppy = nicknames.next();
        assertThat(neppy)
                .hasKey("nicknames")
                .hasParent(vertex)
                .hasValue("Neppy")
                // Tinkerprop does not convert timestamps
                .hasProperty("time", "1970-01-01T00:00:00.022Z");

        VertexProperty<String> flipper = nicknames.next();
        assertThat(flipper)
                .hasKey("nicknames")
                .hasParent(vertex)
                .hasValue("Flipper")
                // Tinkerprop does not convert timestamps
                .hasProperty("time", "1970-01-01T00:00:00.025Z");

        VertexProperty<Integer> age = vertex.property("age");
        assertThat(age.key()).isEqualTo("age");
        assertThat(age.value()).isEqualTo(4500);

        VertexProperty<?> nonExistent = vertex.property("does not exist");
        assertThat(nonExistent.isPresent()).isFalse();
    }
}
