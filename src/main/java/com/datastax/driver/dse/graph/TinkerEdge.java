/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.google.common.collect.ImmutableList;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

import java.util.Iterator;

class TinkerEdge extends TinkerElement implements Edge {

    TinkerVertex inVertex;

    TinkerVertex outVertex;

    TinkerEdge() {
    }

    @Override
    public TinkerVertex inVertex() {
        return inVertex;
    }

    @Override
    public TinkerVertex outVertex() {
        return outVertex;
    }

    @Override
    public Iterator<Vertex> vertices(Direction direction) {
        switch (direction) {
            case OUT:
                return ImmutableList.<Vertex>of(outVertex()).iterator();
            case IN:
                return ImmutableList.<Vertex>of(inVertex()).iterator();
            default:
                return ImmutableList.<Vertex>of(outVertex(), inVertex()).iterator();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <V> Iterator<Property<V>> properties(final String... propertyKeys) {
        return (Iterator) super.properties(propertyKeys);
    }

    @Override
    public void remove() {
        throw Edge.Exceptions.edgeRemovalNotSupported();
    }

    @Override
    public String toString() {
        return StringFactory.edgeString(this);
    }

}
