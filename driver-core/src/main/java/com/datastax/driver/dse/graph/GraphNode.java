/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.dse.serde.Node;

/**
 * A node in a tree-like structure representing a Graph or a Graph component.
 * It contains additional methods that perform conversion to
 * Graph elements (vertex, edge, etc.).
 */
public interface GraphNode extends Node {

    @Override
    GraphNode get(String fieldName);

    @Override
    GraphNode get(int index);

    /**
     * Returns {@code true} if this node is a vertex.
     *
     * If this node is a vertex, then {@link #asVertex()} can be safely called.
     *
     * @return {@code true} if this node is a vertex, {@code false} otherwise.
     */
    boolean isVertex();

    /**
     * Returns {@code true} if this node is an edge.
     *
     * If this node is an edge, then {@link #asEdge()} can be safely called.
     *
     * @return {@code true} if this node is an edge, {@code false} otherwise.
     */
    boolean isEdge();

    /**
     * Returns this node as a {@link Vertex}.
     *
     * @return a {@link Vertex} representation of this node.
     * @throws DriverException if this node cannot be converted to a {@link Vertex}.
     */
    Vertex asVertex();

    /**
     * Returns this node as an {@link Edge}.
     *
     * @return an {@link Edge} representation of this node.
     * @throws DriverException if this node cannot be converted to a {@link Edge}.
     */
    Edge asEdge();

    /**
     * Returns this node as a {@link Path}.
     *
     * @return a {@link Path} representation of this node.
     * @throws DriverException if this node cannot be converted to a {@link Path}.
     */
    Path asPath();

    /**
     * Returns this node as a {@link Property}.
     *
     * @return a {@link Property} representation of this node.
     * @throws DriverException if this node cannot be converted to a {@link Property}.
     */
    Property asProperty();

    /**
     * Returns this node as a {@link VertexProperty}.
     *
     * @return a {@link VertexProperty} representation of this node.
     * @throws DriverException if this node cannot be converted to a {@link VertexProperty}.
     */
    VertexProperty asVertexProperty();

}
