/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import static com.datastax.driver.dse.graph.GraphAssertions.assertThat;

public class EdgeAssert extends ElementAssert<EdgeAssert, Edge> {

    public EdgeAssert(Edge actual) {
        super(actual, EdgeAssert.class);
    }

    public EdgeAssert hasInVLabel(String label) {
        assertThat(actual.getInVLabel()).isEqualTo(label);
        return myself;
    }

    public EdgeAssert hasInV(GraphNode result) {
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

    public EdgeAssert hasOutV(GraphNode result) {
        return hasOutV(result.asVertex());
    }

    public EdgeAssert hasOutV(Vertex v) {
        assertThat(actual.getOutV()).isEqualTo(v.getId());
        return myself;
    }
}
