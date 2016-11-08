/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.google.common.base.Objects;

/**
 * A default representation of an edge in DSE Graph.
 */
class DefaultEdge extends DefaultElement implements Edge {

    GraphNode inV;

    String inVLabel;

    GraphNode outV;

    String outVLabel;

    DefaultEdge() {
    }

    @Override
    public GraphNode getInV() {
        return inV;
    }

    @Override
    public String getInVLabel() {
        return inVLabel;
    }

    @Override
    public GraphNode getOutV() {
        return outV;
    }

    @Override
    public String getOutVLabel() {
        return outVLabel;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", getId())
                .add("label", getLabel())
                .add("properties", getProperties())
                .add("inV", getInV())
                .add("inVLabel", getInVLabel())
                .add("outV", getOutV())
                .add("outVLabel", getOutVLabel())
                .toString();
    }
}
