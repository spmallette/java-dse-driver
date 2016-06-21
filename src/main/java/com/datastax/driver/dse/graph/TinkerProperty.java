/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;

/**
 *
 */
class TinkerProperty<V> implements Property<V> {

    String key;

    V value;

    Element parent;

    TinkerProperty() {
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public String key() {
        return key;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V value() {
        return value;
    }

    @Override
    public Element element() {
        return parent;
    }

    @Override
    public void remove() {
        throw Property.Exceptions.propertyRemovalNotSupported();
    }
}
