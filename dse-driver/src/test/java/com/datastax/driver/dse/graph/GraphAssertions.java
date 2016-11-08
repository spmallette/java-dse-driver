/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

public class GraphAssertions extends com.datastax.driver.core.Assertions {

    public static EdgeAssert assertThat(Edge edge) {
        return new EdgeAssert(edge);
    }

    public static VertexAssert assertThat(Vertex vertex) {
        return new VertexAssert(vertex);
    }

    public static VertexPropertyAssert assertThat(VertexProperty vertexProperty) {
        return new VertexPropertyAssert(vertexProperty);
    }

    public static GraphNodeAssert assertThat(GraphNode result) {
        return new GraphNodeAssert(result);
    }

    public static PathAssert assertThat(Path path) {
        return new PathAssert(path);
    }
}
