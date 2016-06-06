/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

import java.util.Arrays;
import java.util.Iterator;

@SuppressWarnings("Since15")
class TinkerVertex extends TinkerElement implements Vertex {

    TinkerVertex() {
    }

    @Override
    public <V> VertexProperty<V> property(String key, V value) {
        throw Element.Exceptions.propertyAdditionNotSupported();
    }

    @Override
    public <V> VertexProperty<V> property(String key, V value, Object... keyValues) {
        throw Element.Exceptions.propertyAdditionNotSupported();
    }

    @Override
    public <V> VertexProperty<V> property(VertexProperty.Cardinality cardinality, String key, V value, Object... keyValues) {
        throw Element.Exceptions.propertyAdditionNotSupported();
    }

    @Override
    public Edge addEdge(String label, Vertex inVertex, Object... keyValues) {
        throw Vertex.Exceptions.edgeAdditionsNotSupported();
    }

    @Override
    public String toString() {
        return StringFactory.vertexString(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> Iterator<VertexProperty<V>> properties(String... propertyKeys) {
        if (properties == null || properties.isEmpty())
            return Iterators.emptyIterator();
        Predicate<String> containsKey = Predicates.in(Arrays.asList(propertyKeys));
        Multimap<String, ? extends Property<?>> filtered = Multimaps.filterKeys(properties, containsKey);
        return (Iterator<VertexProperty<V>>) Iterators.unmodifiableIterator(filtered.values().iterator());
    }

    @Override
    public Iterator<Edge> edges(Direction direction, String... edgeLabels) {
        return Iterators.emptyIterator();
    }

    @Override
    public Iterator<Vertex> vertices(Direction direction, String... labels) {
        return Iterators.emptyIterator();
    }

    @Override
    public void remove() {
        throw Vertex.Exceptions.vertexRemovalNotSupported();
    }

}
