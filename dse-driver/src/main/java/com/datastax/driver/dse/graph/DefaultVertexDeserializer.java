/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.collect.MultimapBuilder;

import java.io.IOException;

import static com.fasterxml.jackson.core.JsonToken.*;

class DefaultVertexDeserializer extends StdDeserializer<DefaultVertex> {

    DefaultVertexDeserializer() {
        super(DefaultVertex.class);
    }

    @Override
    public DefaultVertex deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        assert parser.getCurrentToken() == START_OBJECT;
        DefaultVertex vertex = new DefaultVertex();
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
                parser.nextToken();
                parser.skipChildren();
            }
        }
        return vertex;
    }

    private DefaultVertex readId(DefaultVertex vertex, JsonParser parser, DeserializationContext ctx) throws IOException {
        JsonNode jacksonNode = parser.readValueAsTree();
        vertex.id = new DefaultGraphNode(jacksonNode, (ObjectMapper) parser.getCodec());
        return vertex;
    }

    private DefaultVertex readLabel(DefaultVertex vertex, JsonParser parser, DeserializationContext ctx) throws IOException {
        vertex.label = parser.readValueAs(String.class);
        return vertex;
    }

    private DefaultVertex readType(DefaultVertex vertex, JsonParser parser, DeserializationContext ctx) throws IOException {
        JsonLocation currentLocation = parser.getCurrentLocation();
        String type = parser.readValueAs(String.class);
        if (type == null || !type.equals("vertex"))
            throw new JsonParseException(String.format("Expected 'vertex' type, got '%s'", type), currentLocation);
        return vertex;
    }

    private DefaultVertex readProperty(DefaultVertex vertex, String propertyName, JsonParser parser, DeserializationContext ctx) throws IOException {
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
            JsonNode jacksonNode = parser.readValueAsTree();
            PropertyGraphNode propertyValue = new PropertyGraphNode(jacksonNode, (ObjectMapper) parser.getCodec(), propertyName, vertex);
            vertex.properties.put(propertyName, propertyValue);
        }
        return vertex;
    }

}
