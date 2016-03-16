/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import org.apache.tinkerpop.gremlin.structure.Property;

import java.io.IOException;
import java.util.Iterator;

import static com.fasterxml.jackson.core.JsonToken.*;

class TinkerVertexDeserializer extends StdDeserializer<TinkerVertex> {

    private static final String ID = "id";
    private static final String VALUE = "value";
    private static final String PROPERTIES = "properties";

    TinkerVertexDeserializer() {
        super(TinkerVertex.class);
    }

    @Override
    public TinkerVertex deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        assert parser.getCurrentToken() == START_OBJECT;
        TinkerVertex vertex = new TinkerVertex();
        while (parser.nextToken() != END_OBJECT) {
            assert parser.getCurrentToken() == FIELD_NAME;
            String name = parser.getCurrentName();
            parser.nextToken();
            if ("id".equals(name)) {
                assert parser.getCurrentToken() == START_OBJECT;
                vertex = readId(vertex, parser, ctx);
            } else if ("label".equals(name)) {
                assert parser.getCurrentToken() == VALUE_STRING;
                vertex = readLabel(vertex, parser, ctx);
            } else if ("type".equals(name)) {
                assert parser.getCurrentToken() == VALUE_STRING;
                vertex = readType(vertex, parser, ctx);
            } else if ("properties".equals(name)) {
                assert parser.getCurrentToken() == START_OBJECT;
                while (parser.nextToken() != END_OBJECT) {
                    assert parser.getCurrentToken() == FIELD_NAME;
                    String key = parser.getCurrentName();
                    parser.nextToken();
                    vertex = readProperty(vertex, key, parser, ctx);
                }
            } else {
                parser.skipChildren();
            }
        }
        return vertex;
    }

    private TinkerVertex readId(TinkerVertex vertex, JsonParser parser, DeserializationContext ctx) throws IOException {
        vertex.id = parser.readValueAs(Object.class);
        return vertex;
    }

    private TinkerVertex readLabel(TinkerVertex vertex, JsonParser parser, DeserializationContext ctx) throws IOException {
        vertex.label = parser.readValueAs(String.class);
        return vertex;
    }

    private TinkerVertex readType(TinkerVertex vertex, JsonParser parser, DeserializationContext ctx) throws IOException {
        JsonLocation currentLocation = parser.getCurrentLocation();
        String type = parser.readValueAs(String.class);
        if (type == null || !type.equals("vertex"))
            throw new JsonParseException(String.format("Expected 'vertex' type, got '%s'", type), currentLocation);
        return vertex;
    }

    private TinkerVertex readProperty(TinkerVertex vertex, String propertyName, JsonParser parser, DeserializationContext ctx) throws IOException {
        if (vertex.properties == null) {
            vertex.properties = MultimapBuilder
                    .linkedHashKeys()
                    .linkedHashSetValues()
                    .build();
        }
        // vertex properties can be multivaluated
        assert parser.getCurrentToken() == START_ARRAY;
        while (parser.nextToken() != END_ARRAY) {
            assert parser.getCurrentToken() == START_OBJECT;
            JsonNode propertyNode = parser.readValueAsTree();
            ObjectMapper objectMapper = (ObjectMapper) parser.getCodec();
            TinkerVertexProperty<Object> vertexProperty = new TinkerVertexProperty<Object>();
            vertexProperty.id = objectMapper.treeToValue(propertyNode.get(ID), Object.class);
            vertexProperty.label = propertyName;
            vertexProperty.value = objectMapper.treeToValue(propertyNode.get(VALUE), Object.class);
            vertexProperty.parent = vertex;
            vertexProperty.properties = metaProperties(propertyNode, objectMapper, vertexProperty);
            vertex.properties.put(propertyName, vertexProperty);
        }
        return vertex;
    }

    private Multimap<String, Property<Object>> metaProperties(JsonNode propertyNode, ObjectMapper objectMapper, TinkerVertexProperty<Object> vertexProperty) throws JsonProcessingException {
        if (propertyNode == null)
            return null;
        JsonNode metaProperties = propertyNode.get(PROPERTIES);
        if (metaProperties == null)
            return null;
        SetMultimap<String, Property<Object>> metaPropertiesMap =
                MultimapBuilder.linkedHashKeys().linkedHashSetValues().build();
        Iterator<String> it = metaProperties.fieldNames();
        while (it.hasNext()) {
            TinkerProperty<Object> metaProperty = new TinkerProperty<Object>();
            metaProperty.key = it.next();
            metaProperty.value = objectMapper.treeToValue(metaProperties.get(metaProperty.key), Object.class);
            metaProperty.parent = vertexProperty;
            metaPropertiesMap.put(metaProperty.key, metaProperty);
        }
        return metaPropertiesMap;
    }

}
