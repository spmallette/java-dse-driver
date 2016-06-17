/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

public class TinkerGraphAssertions extends GraphAssertions {

    public static TinkerEdgeAssert assertThat(Edge edge) {
        return new TinkerEdgeAssert(edge);
    }

    public static TinkerVertexAssert assertThat(Vertex vertex) {
        return new TinkerVertexAssert(vertex);
    }

    public static <T> TinkerVertexPropertyAssert<T> assertThat(VertexProperty<T> vertexProperty) {
        return new TinkerVertexPropertyAssert<T>(vertexProperty);
    }

    public static TinkerPathAssert assertThat(Path path) {
        return new TinkerPathAssert(path);
    }
}
