/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import org.assertj.core.api.AbstractAssert;

import static com.datastax.driver.dse.graph.GraphAssertions.assertThat;

public class GraphNodeAssert extends AbstractAssert<GraphNodeAssert, GraphNode> {

    public GraphNodeAssert(GraphNode actual) {
        super(actual, GraphNodeAssert.class);
    }

    public EdgeAssert asEdge() {
        return new EdgeAssert(actual.asEdge());
    }

    public VertexAssert asVertex() {
        return new VertexAssert(actual.asVertex());
    }

    public GraphNodeAssert hasChild(String key) {
        assertThat(actual.get(key).isNull()).isFalse();
        return myself;
    }

    public GraphNodeAssert hasChild(int index) {
        assertThat(actual.get(index).isNull()).isFalse();
        return myself;
    }

    public GraphNodeAssert child(String key) {
        hasChild(key);
        return new GraphNodeAssert(actual.get(key));
    }

    public GraphNodeAssert child(int index) {
        hasChild(index);
        return new GraphNodeAssert(actual.get(index));
    }

}
