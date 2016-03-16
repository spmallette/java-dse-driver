/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;

/**
 * A special {@link TreeTraversingParser} that specializes
 * in parsing {@link PropertyGraphNode property nodes};
 * it keeps track of the node's parent element and property name.
 * Used only internally by property deserializers.
 *
 * @see DefaultPropertyDeserializer
 * @see DefaultVertexPropertyDeserializer
 */
class PropertyGraphNodeParser extends TreeTraversingParser {

    final String propertyName;

    final Element parent;

    PropertyGraphNodeParser(JsonNode delegate, ObjectMapper objectMapper, String propertyName, Element parent) {
        super(delegate, objectMapper);
        this.propertyName = propertyName;
        this.parent = parent;
    }

}
