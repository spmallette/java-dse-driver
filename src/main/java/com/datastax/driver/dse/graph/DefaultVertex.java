/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import java.util.Iterator;

/**
 * A default representation of a vertex in DSE graph.
 */
class DefaultVertex extends DefaultElement implements Vertex {

    private static final Function<GraphNode, VertexProperty> GRAPH_NODE_TO_VERTEX_PROPERTY = new Function<GraphNode, VertexProperty>() {
        @Override
        public VertexProperty apply(GraphNode input) {
            return input.asVertexProperty();
        }
    };

    DefaultVertex() {
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Vertex && super.equals(o);
    }

    @Override
    public VertexProperty getProperty(String name) {
        if (properties == null || properties.isEmpty() || !properties.containsKey(name))
            return null;
        Iterator<VertexProperty> iterator = getProperties(name);
        if (!iterator.hasNext())
            return null;
        return iterator.next();
    }

    @Override
    public Iterator<VertexProperty> getProperties(String name) {
        if (properties == null || properties.isEmpty() || !properties.containsKey(name))
            return Iterators.emptyIterator();
        return Iterators.unmodifiableIterator(Iterators.transform(properties.get(name).iterator(), GRAPH_NODE_TO_VERTEX_PROPERTY));
    }

    @Override
    public Iterator<VertexProperty> getProperties() {
        if (properties == null || properties.isEmpty())
            return Iterators.emptyIterator();
        return Iterators.unmodifiableIterator(Iterators.transform(properties.values().iterator(), GRAPH_NODE_TO_VERTEX_PROPERTY));
    }

}
