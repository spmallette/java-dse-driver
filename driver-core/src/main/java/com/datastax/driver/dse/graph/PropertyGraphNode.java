/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.exceptions.DriverException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.google.common.reflect.TypeToken;

/**
 * A node that represents a Graph property.
 * <p>
 * Such nodes can only appear as fields inside JSON objects,
 * and this implementation allows to retrieve
 * its parent element and its corresponding property name
 * (which corresponds to the field name in the parent JSON object).
 *
 * @see DefaultPropertyDeserializer
 * @see DefaultVertexPropertyDeserializer
 */
class PropertyGraphNode extends DefaultGraphNode {

    private final String propertyName;

    private final Element parent;

    PropertyGraphNode(JsonNode delegate, ObjectMapper objectMapper, String propertyName, Element parent) {
        super(delegate, objectMapper);
        this.propertyName = propertyName;
        this.parent = parent;
    }

    @Override
    public <T> T as(Class<T> clazz) {
        return as(TypeToken.of(clazz));
    }

    @Override
    public <T> T as(TypeToken<T> type) {
        try {
            final TreeTraversingParser parser = new PropertyGraphNodeParser(delegate, objectMapper, propertyName, parent);
            JavaType javaType = objectMapper.constructType(type.getType());
            return objectMapper.readValue(parser, javaType);
        } catch (Exception e) {
            throw new DriverException("Cannot deserialize result as " + type, e);
        }
    }

}
