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

import static com.datastax.driver.dse.graph.GraphAssertions.assertThat;
import static com.google.common.base.Charsets.UTF_8;

@SuppressWarnings("unchecked")
public class DefaultPathDeserializerTest {
    // Expected graphson for a traversal on the modern GraphFixture: g.V().hasLabel("person").has("name", "marko").as("a").outE("knows").as("b").inV().as("c", "d").outE("created").as("e", "f", "g").inV().as("a").path()

    @Test(groups = "unit")
    public void should_deserialize_as_path_graphson_1_0() throws Exception {
        Path path = GraphJsonUtils.readStringAsTree(Resources.toString(getClass().getResource("/graphson-1.0/path1.json"), UTF_8)).as(Path.class);
        assertPath(path);
    }

    @Test(groups = "unit")
    public void should_deserialize_as_path_graphson_2_0() throws Exception {
        Path path = GraphJsonUtils.readStringAsTreeGraphson20(Resources.toString(getClass().getResource("/graphson-2.0/path1.json"), UTF_8)).as(Path.class);
        assertPath(path);
    }

    void assertPath(Path path) throws IOException {
        PathAssert pathAssert = assertThat(path)
                .isInstanceOf(DefaultPath.class)
                .hasLabel("a")
                .hasLabel("b")
                .hasLabel("c")
                .hasLabel("d")
                .hasLabel("e")
                .hasLabel("f")
                .hasLabel("g")
                .hasLabel(4, "a")
                .hasLabel(0, "a")
                .hasLabel(1, "b")
                .hasLabel(2, "c", "d")
                .hasLabel(3, "e", "f", "g")
                .hasLabel(4, "a")
                .doesNotHaveLabel("h");

        assertThat(path.getObjects()).hasSize(5);
        assertThat(path.size()).isEqualTo(5);

        pathAssert.object(0).asVertex()
                .hasLabel("person")
                .hasProperty("name", "marko")
                .hasProperty("age", 29);

        pathAssert.object(1).asEdge()
                .hasLabel("knows")
                .hasInVLabel("person")
                .hasOutVLabel("person")
                .hasProperty("weight", 1.0f);

        pathAssert.object(2).asVertex()
                .hasLabel("person")
                .hasProperty("name", "josh")
                .hasProperty("age", 32);

        pathAssert.object(3).asEdge()
                .hasLabel("created")
                .hasInVLabel("software")
                .hasOutVLabel("person")
                .hasProperty("weight", 1.0f);

        pathAssert.object(4).asVertex()
                .hasLabel("software")
                .hasProperty("name", "ripple")
                .hasProperty("lang", "java");

        pathAssert.object("a").asVertex()
                .hasLabel("person")
                .hasProperty("name", "marko")
                .hasProperty("age", 29);

        pathAssert.objects("a").hasSize(2);

        assertThat(path.getObjects("a").get(0).asVertex())
                .hasLabel("person")
                .hasProperty("name", "marko")
                .hasProperty("age", 29);

        assertThat(path.getObjects("a").get(1).asVertex())
                .hasLabel("software")
                .hasProperty("name", "ripple")
                .hasProperty("lang", "java");

        assertThat(path.getObject("nonexistent")).isNull();
        assertThat(path.getObject(5)).isNull();

    }
}
