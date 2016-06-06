/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import java.util.Iterator;

/**
 * The representation of a vertex in DSE Graph.
 */
public interface Vertex extends Element {

    /**
     * {@inheritDoc}
     */
    @Override
    VertexProperty getProperty(String name);

    /**
     * {@inheritDoc}
     */
    @Override
    Iterator<VertexProperty> getProperties(String name);

    /**
     * {@inheritDoc}
     */
    @Override
    Iterator<VertexProperty> getProperties();

}
