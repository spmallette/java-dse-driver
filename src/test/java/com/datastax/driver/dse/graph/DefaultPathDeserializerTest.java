/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.google.common.io.Resources;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.datastax.driver.dse.graph.GraphAssertions.assertThat;
import static com.google.common.base.Charsets.UTF_8;

@SuppressWarnings("unchecked")
public class DefaultPathDeserializerTest {

    @Test(groups = "unit")
    public void should_deserialize_as_path() throws Exception {
        Path path = GraphJsonUtils.INSTANCE.readStringAsTree(Resources.toString(getClass().getResource("/path1.json"), UTF_8)).as(Path.class);
        assertPath(path);
    }

    void assertPath(Path path) throws IOException {
        PathAssert pathAssert = assertThat(path)
                .isInstanceOf(DefaultPath.class)
                .hasLabel(0, "a")
                .hasLabel(1, "b")
                .hasLabel(2, "c", "d")
                .hasLabel(3, "e", "f", "g")
                .hasLabel(4, "h");

        assertThat(path.getObjects()).hasSize(5);

        pathAssert.object(0).asVertex()
                .hasLabel("person")
                .hasProperty("name", "marko")
                .hasProperty("age", 29);

        pathAssert.object(1).asEdge()
                .hasLabel("knows")
                .hasInVLabel("person")
                .hasOutVLabel("person")
                .hasProperty("weight", 1.0);

        pathAssert.object(2).asVertex()
                .hasLabel("person")
                .hasProperty("name", "josh")
                .hasProperty("age", 32);

        pathAssert.object(3).asEdge()
                .hasLabel("created")
                .hasInVLabel("software")
                .hasOutVLabel("person")
                .hasProperty("weight", 1.0);

        pathAssert.object(4).asVertex()
                .hasLabel("software")
                .hasProperty("name", "ripple")
                .hasProperty("lang", "java");

    }
}
