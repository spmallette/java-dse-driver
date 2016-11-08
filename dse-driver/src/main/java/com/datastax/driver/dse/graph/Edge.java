/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

/**
 * The representation of an edge in DSE Graph.
 */
public interface Edge extends Element {

    /**
     * Returns the identifier of this edge's incoming/head vertex.
     *
     * @return the vertex.
     */
    GraphNode getInV();

    /**
     * Returns the label of this edge's incoming/head vertex.
     *
     * @return the label.
     */
    String getInVLabel();

    /**
     * Returns the identifier of this edge's outgoing/tail vertex.
     *
     * @return the vertex.
     */
    GraphNode getOutV();

    /**
     * Returns the label of this edge's outgoing/tail vertex.
     *
     * @return the label.
     */
    String getOutVLabel();

}
