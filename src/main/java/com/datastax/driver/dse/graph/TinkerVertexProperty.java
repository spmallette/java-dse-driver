/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import java.util.Iterator;

/**
 *
 */
class TinkerVertexProperty<V> extends TinkerElement implements VertexProperty<V> {

    V value;

    Vertex parent;

    TinkerVertexProperty() {
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public String key() {
        return label();
    }

    @SuppressWarnings("unchecked")
    @Override
    public V value() {
        return value;
    }

    @Override
    public Vertex element() {
        return parent;
    }

    @Override
    public <U> Property<U> property(String key, U value) {
        throw Element.Exceptions.propertyAdditionNotSupported();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> Iterator<Property<U>> properties(final String... propertyKeys) {
        return (Iterator) super.properties(propertyKeys);
    }

    @Override
    public void remove() {
        throw Property.Exceptions.propertyRemovalNotSupported();
    }

}
