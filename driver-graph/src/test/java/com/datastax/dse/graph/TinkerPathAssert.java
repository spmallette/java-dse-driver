/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph;

import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractObjectAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class TinkerPathAssert extends AbstractAssert<TinkerPathAssert, Path> {

    public TinkerPathAssert(Path actual) {
        super(actual, TinkerPathAssert.class);
    }

    public AbstractObjectAssert<?, Object> objectAt(int i) {
        assertThat(actual.size()).isGreaterThanOrEqualTo(i);
        return assertThat(actual.objects().get(i));
    }

    public TinkerVertexAssert vertexAt(int i) {
        assertThat(actual.size()).isGreaterThanOrEqualTo(i);
        Object o = actual.objects().get(i);
        assertThat(o).isInstanceOf(Vertex.class);
        return new TinkerVertexAssert((Vertex) o);
    }

    public TinkerEdgeAssert edgeAt(int i) {
        assertThat(actual.size()).isGreaterThanOrEqualTo(i);
        Object o = actual.objects().get(i);
        assertThat(o).isInstanceOf(Edge.class);
        return new TinkerEdgeAssert((Edge) o);
    }

    public TinkerPathAssert hasLabel(int i, String... labels) {
        assertThat(actual.labels().size()).isGreaterThanOrEqualTo(i);
        assertThat(actual.labels().get(i)).containsExactly(labels);
        return myself;
    }

    public TinkerPathAssert hasNoLabel(int i) {
        assertThat(actual.labels().size()).isGreaterThanOrEqualTo(i);
        assertThat(actual.labels().get(i)).isEmpty();
        return myself;
    }

    public TinkerPathAssert doesNotHaveLabel(String label) {
        assertThat(actual.hasLabel(label)).isFalse();
        return myself;
    }
}
