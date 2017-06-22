/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.exceptions.DriverException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.reflect.TypeToken;

import java.util.Iterator;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The default implementation of {@link GraphNode} used by the driver.
 */
class DefaultGraphNode implements GraphNode {

    private static final String TYPE = "type";
    private static final String VERTEX_TYPE = "vertex";
    private static final String EDGE_TYPE = "edge";

    private static final TypeToken<Map<String, Object>> MAP_TYPE = new TypeToken<Map<String, Object>>() {
    };

    final JsonNode delegate;

    final ObjectMapper objectMapper;

    DefaultGraphNode(JsonNode delegate, ObjectMapper objectMapper) {
        checkNotNull(delegate);
        checkNotNull(objectMapper);
        this.delegate = delegate;
        this.objectMapper = objectMapper;
    }

    @Override
    public DefaultGraphNode get(String fieldName) {
        JsonNode node = delegate.get(fieldName);
        if (node == null)
            return null;
        return new DefaultGraphNode(node, objectMapper);
    }

    @Override
    public DefaultGraphNode get(int index) {
        JsonNode node = delegate.get(index);
        if (node == null)
            return null;
        return new DefaultGraphNode(node, objectMapper);
    }

    @Override
    public int asInt() {
        return delegate.asInt();
    }

    @Override
    public boolean asBoolean() {
        return delegate.asBoolean();
    }

    @Override
    public long asLong() {
        return delegate.asLong();
    }

    @Override
    public double asDouble() {
        return delegate.asDouble();
    }

    @Override
    public String asString() {
        return delegate.asText();
    }

    @Override
    public Map<String, Object> asMap() {
        return as(MAP_TYPE);
    }

    @Override
    public boolean isVertex() {
        JsonNode type = delegate.get(TYPE);
        return type != null && VERTEX_TYPE.equals(type.asText());
    }

    @Override
    public boolean isEdge() {
        JsonNode type = delegate.get(TYPE);
        return type != null && EDGE_TYPE.equals(type.asText());
    }

    @Override
    public Vertex asVertex() {
        return as(Vertex.class);
    }

    @Override
    public Edge asEdge() {
        return as(Edge.class);
    }

    @Override
    public Path asPath() {
        return as(Path.class);
    }

    @Override
    public Property asProperty() {
        return as(Property.class);
    }

    @Override
    public VertexProperty asVertexProperty() {
        return as(VertexProperty.class);
    }

    @Override
    public <T> T as(Class<T> clazz) {
        try {
            return objectMapper.treeToValue(delegate, clazz);
        } catch (Exception e) {
            throw new DriverException("Cannot deserialize node as " + clazz, e);
        }
    }

    @Override
    public <T> T as(TypeToken<T> type) {
        try {
            JsonParser parser = objectMapper.treeAsTokens(delegate);
            JavaType javaType = objectMapper.constructType(type.getType());
            return objectMapper.readValue(parser, javaType);
        } catch (Exception e) {
            throw new DriverException("Cannot deserialize node as " + type, e);
        }
    }

    @Override
    public boolean isNull() {
        return delegate.isNull();
    }

    @Override
    public boolean isObject() {
        return delegate.isObject();
    }

    @Override
    public boolean isArray() {
        return delegate.isArray();
    }

    @Override
    public boolean isValue() {
        return delegate.isValueNode();
    }

    @Override
    public Iterator<String> fieldNames() {
        return delegate.fieldNames();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultGraphNode)) return false;
        DefaultGraphNode that = (DefaultGraphNode) o;
        return Objects.equal(delegate, that.delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(delegate);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
