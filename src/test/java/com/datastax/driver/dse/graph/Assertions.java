/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

public class Assertions extends com.datastax.driver.core.Assertions {

    public static EdgeAssert assertThat(Edge edge) {
        return new EdgeAssert(edge);
    }

    public static VertexAssert assertThat(Vertex vertex) {
        return new VertexAssert(vertex);
    }

    public static GraphResultAssert assertThat(GraphResult result) {
        return new GraphResultAssert(result);
    }

    public static PathAssert assertThat(Path path) {
        return new PathAssert(path);
    }
}
