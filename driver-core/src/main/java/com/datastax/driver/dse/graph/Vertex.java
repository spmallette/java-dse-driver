/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
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
