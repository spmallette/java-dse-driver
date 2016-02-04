/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import static com.datastax.driver.dse.graph.Assertions.assertThat;

public class EdgeAssert extends ElementAssert<EdgeAssert, Edge> {
    protected EdgeAssert(Edge actual) {
        super(actual, EdgeAssert.class);
    }

    public EdgeAssert hasInVLabel(String label) {
        assertThat(actual.getInVLabel()).isEqualTo(label);
        return myself;
    }

    public EdgeAssert hasInV(GraphResult result) {
        return hasInV(result.asVertex());
    }

    public EdgeAssert hasInV(Vertex v) {
        assertThat(actual.getInV()).isEqualTo(v.getId());
        return myself;
    }

    public EdgeAssert hasOutVLabel(String label) {
        assertThat(actual.getOutVLabel()).isEqualTo(label);
        return myself;
    }

    public EdgeAssert hasOutV(GraphResult result) {
        return hasOutV(result.asVertex());
    }

    public EdgeAssert hasOutV(Vertex v) {
        assertThat(actual.getOutV()).isEqualTo(v.getId());
        return myself;
    }
}
