/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import org.assertj.core.api.AbstractAssert;

import static com.datastax.driver.dse.graph.Assertions.assertThat;

public class GraphResultAssert extends AbstractAssert<GraphResultAssert, GraphResult> {
    protected GraphResultAssert(GraphResult actual) {
        super(actual, GraphResultAssert.class);
    }

    public EdgeAssert asEdge() {
        return new EdgeAssert(actual.asEdge());
    }

    public VertexAssert asVertex() {
        return new VertexAssert(actual.asVertex());
    }

    public GraphResultAssert hasChild(String key) {
        assertThat(actual.get(key).isNull()).isFalse();
        return myself;
    }

    public GraphResultAssert hasChild(int index) {
        assertThat(actual.get(index).isNull()).isFalse();
        return myself;
    }

    public GraphResultAssert child(String key) {
        hasChild(key);
        return new GraphResultAssert(actual.get(key));
    }

    public GraphResultAssert child(int index) {
        hasChild(index);
        return new GraphResultAssert(actual.get(index));
    }
}
