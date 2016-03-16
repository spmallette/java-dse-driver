/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;

import java.util.Arrays;
import java.util.Iterator;

abstract class TinkerElement implements Element {

    Object id;

    String label;

    Multimap<String, Property<Object>> properties;

    TinkerElement() {
    }

    @Override
    public Graph graph() {
        return EmptyGraph.instance();
    }

    @Override
    public Object id() {
        return id;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public <V> Property<V> property(String key, V value) {
        throw Element.Exceptions.propertyAdditionNotSupported();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> Iterator<? extends Property<V>> properties(String... propertyKeys) {
        if (properties == null || properties.isEmpty())
            return Iterators.emptyIterator();
        Predicate<String> containsKey = Predicates.in(Arrays.asList(propertyKeys));
        Multimap<String, ? extends Property<?>> filtered = Multimaps.filterKeys(properties, containsKey);
        return (Iterator<? extends Property<V>>) Iterators.unmodifiableIterator(filtered.values().iterator());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Element)) return false;
        Element that = (Element) o;
        // we compare only ids to be consistent with Tinkerpop
        return Objects.equal(this.id(), that.id());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id());
    }

}
