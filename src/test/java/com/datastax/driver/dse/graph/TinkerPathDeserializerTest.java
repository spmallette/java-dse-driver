/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.google.common.io.Resources;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.datastax.driver.dse.graph.TinkerGraphAssertions.assertThat;
import static com.google.common.base.Charsets.UTF_8;


@SuppressWarnings("unchecked")
public class TinkerPathDeserializerTest {

    @Test(groups = "unit")
    public void should_deserialize_as_tinker_path() throws Exception {
        Path path = GraphJsonUtils.INSTANCE.readStringAsTree(Resources.toString(getClass().getResource("/path1.json"), UTF_8)).as(Path.class);
        assertPath(path);
    }

    void assertPath(Path path) throws IOException {
        assertThat(path)
                .hasLabel(0, "a")
                .hasLabel(1, "b")
                .hasLabel(2, "c", "d")
                .hasLabel(3, "e", "f", "g")
                .hasLabel(4, "h");

        assertThat(path.objects()).hasSize(5);
        assertThat(path.size()).isEqualTo(5);

        assertThat(path)
                .vertexAt(0)
                .hasLabel("person")
                .hasProperty("name", "marko")
                .hasProperty("age", 29);

        assertThat(path)
                .edgeAt(1)
                .hasLabel("knows")
                .hasInVLabel("person")
                .hasOutVLabel("person")
                .hasProperty("weight", 1.0);

        assertThat(path)
                .vertexAt(2)
                .hasLabel("person")
                .hasProperty("name", "josh")
                .hasProperty("age", 32);

        assertThat(path)
                .edgeAt(3)
                .hasLabel("created")
                .hasInVLabel("software")
                .hasOutVLabel("person")
                .hasProperty("weight", 1.0);

        assertThat(path)
                .vertexAt(4)
                .hasLabel("software")
                .hasProperty("name", "ripple")
                .hasProperty("lang", "java");

        assertThat(path.get("a")).isEqualTo(path.get(0));
        assertThat(path.get("b")).isEqualTo(path.get(1));
        assertThat(path.hasLabel("b")).isTrue();

    }
}
