/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;

import java.io.IOException;
import java.util.Iterator;

class DefaultVertexPropertyDeserializer extends StdDeserializer<DefaultVertexProperty> {

    private static final String ID = "id";
    private static final String VALUE = "value";
    private static final String PROPERTIES = "properties";

    DefaultVertexPropertyDeserializer() {
        super(VertexProperty.class);
    }

    @Override
    public DefaultVertexProperty deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        if (!(parser instanceof PropertyGraphNodeParser))
            throw new JsonParseException("Cannot deserialize property if parser is not instance of PropertyGraphNodeParser", parser.getCurrentLocation());
        PropertyGraphNodeParser propertyGraphNodeParser = (PropertyGraphNodeParser) parser;
        String name = propertyGraphNodeParser.propertyName;
        if (name == null)
            throw new JsonParseException("Cannot deserialize property with null name", parser.getCurrentLocation());
        Element parent = propertyGraphNodeParser.parent;
        if (parent == null)
            throw new JsonParseException("Cannot deserialize property with null parent", parser.getCurrentLocation());
        if (!(parent instanceof Vertex))
            throw new JsonParseException("Vertex property parent is not a Vertex: " + parent, parser.getCurrentLocation());
        JsonNode jacksonNode = parser.readValueAsTree();
        ObjectMapper objectMapper = (ObjectMapper) parser.getCodec();
        DefaultGraphNode propertyNode = new DefaultGraphNode(jacksonNode, objectMapper);
        DefaultVertexProperty vertexProperty = new DefaultVertexProperty();
        vertexProperty.id = propertyNode.get(ID);
        vertexProperty.label = name;
        vertexProperty.value = propertyNode.get(VALUE);
        vertexProperty.parent = (Vertex) parent;
        vertexProperty.properties = metaProperties(propertyNode, objectMapper, vertexProperty);
        return vertexProperty;
    }

    private Multimap<String, GraphNode> metaProperties(DefaultGraphNode node, ObjectMapper objectMapper, DefaultVertexProperty vertexProperty) {
        if (node == null)
            return null;
        GraphNode metaProperties = node.get(PROPERTIES);
        if (metaProperties == null)
            return null;
        SetMultimap<String, GraphNode> metaPropertiesMap =
                MultimapBuilder.linkedHashKeys().linkedHashSetValues().build();
        Iterator<String> it = metaProperties.fieldNames();
        while (it.hasNext()) {
            String metaPropKey = it.next();
            GraphNode metaPropValue = metaProperties.get(metaPropKey);
            metaPropertiesMap.put(metaPropKey, new PropertyGraphNode(
                    ((DefaultGraphNode) metaPropValue).delegate,
                    objectMapper,
                    metaPropKey,
                    vertexProperty)
            );
        }
        return metaPropertiesMap;
    }

}
