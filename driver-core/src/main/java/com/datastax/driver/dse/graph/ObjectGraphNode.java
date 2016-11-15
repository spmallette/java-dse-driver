/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.google.common.base.Objects;
import com.google.common.reflect.TypeToken;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ObjectGraphNode implements GraphNode {

    private final Object delegate;

    public ObjectGraphNode(Object delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isNull() {
        return delegate == null;
    }

    @Override
    public boolean isObject() {
        return delegate instanceof Map;
    }

    @Override
    public boolean isArray() {
        return delegate instanceof List;
    }

    @Override
    public boolean isValue() {
        return !(isArray() || isObject());
    }

    @Override
    public Iterator<String> fieldNames() {
        return ((Map<String, ?>)delegate).keySet().iterator();
    }

    @Override
    public int size() {
        return isArray() ? ((List) delegate).size() : 0;
    }

    @Override
    public int asInt() {
        return (Integer) delegate;
    }

    @Override
    public boolean asBoolean() {
        return (Boolean) delegate;
    }

    @Override
    public long asLong() {
        return (Long) delegate;
    }

    @Override
    public double asDouble() {
        return (Double) delegate;
    }

    @Override
    public String asString() {
        return (String) delegate;
    }

    @Override
    public Map<String, Object> asMap() {
        return (Map<String, Object>) delegate;
    }

    @Override
    public <T> T as(Class<T> clazz) {
        return (T) delegate;
    }

    @Override
    public <T> T as(TypeToken<T> type) {
        return (T) delegate;
    }

    @Override
    public GraphNode get(String fieldName) {
        Object fieldValue = ((Map) delegate).get(fieldName);
        if (fieldValue == null) {
            return null;
        } else {
            return new ObjectGraphNode(fieldValue);
        }
    }

    @Override
    public GraphNode get(int index) {
        Object indexValue = ((List) delegate).get(index);
        if (indexValue == null) {
            return null;
        } else {
            return new ObjectGraphNode(indexValue);
        }
    }
    @Override
    public boolean isVertex() {
        return delegate instanceof Vertex;
    }

    @Override
    public boolean isEdge() {
        return delegate instanceof Edge;
    }

    @Override
    public Vertex asVertex() {
        return (Vertex) delegate;
    }

    @Override
    public Edge asEdge() {
        return (Edge) delegate;
    }

    @Override
    public Path asPath() {
        return (Path) delegate;
    }

    @Override
    public Property asProperty() {
        return (Property) delegate;
    }

    @Override
    public VertexProperty asVertexProperty() {
        return (VertexProperty) delegate;
    }

    @Override
    public String toString() {
        return this.delegate.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        // Compare each others' delegates.
        return other instanceof ObjectGraphNode &&
                Objects.equal(this.delegate, ((ObjectGraphNode) other).delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(delegate);
    }

}
