/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.google.common.io.Resources;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Pop;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.datastax.driver.dse.graph.TinkerGraphAssertions.assertThat;
import static com.google.common.base.Charsets.UTF_8;
import static org.assertj.core.api.Assertions.fail;

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
                .hasLabel(4, "a")
                .doesNotHaveLabel("h");

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

        assertThat(path.get("a")).isInstanceOf(List.class);
        List<Vertex> list = path.get("a");
        assertThat(list).hasSize(2);

        assertThat(list.get(0))
                .hasLabel("person")
                .hasProperty("name", "marko")
                .hasProperty("age", 29);

        assertThat(list.get(1))
                .hasLabel("software")
                .hasProperty("name", "ripple")
                .hasProperty("lang", "java");

        assertThat(list.get(0)).isEqualTo(path.get(0));
        assertThat(list.get(1)).isEqualTo(path.get(4));

        assertThat(path.hasLabel("b")).isTrue();
        assertThat(path.get("b")).isEqualTo(path.get(1));

        assertThat(path.iterator()).hasSize(5);
        assertThat(path.labels()).hasSize(5);

        try {
            path.get("nonexistent");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            path.get(5);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // ok
        }

        assertThat(path.get(Pop.all, "a")).isEqualTo(list);
        assertThat(path.get(Pop.first, "a")).isEqualTo(list.get(0));
        assertThat(path.get(Pop.last, "a")).isEqualTo(list.get(1));

        assertThat(path.get(Pop.all, "b")).isEqualTo(Collections.singletonList(path.get(1)));
        assertThat(path.get(Pop.first, "b")).isEqualTo(path.get(1));
        assertThat(path.get(Pop.last, "b")).isEqualTo(path.get(1));

        //noinspection RedundantCast
        assertThat((Path) path.clone()).isEqualTo(path);
    }
}
