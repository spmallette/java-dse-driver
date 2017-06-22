/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.collect.MultimapBuilder;

import java.io.IOException;

import static com.fasterxml.jackson.core.JsonToken.*;

/**
 * Default edge deserializer used by the driver.
 * It deserializes edges into {@link DefaultEdge} instances.
 */
class DefaultEdgeDeserializer extends StdDeserializer<DefaultEdge> {

    DefaultEdgeDeserializer() {
        super(DefaultEdge.class);
    }

    @Override
    public DefaultEdge deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        assert parser.getCurrentToken() == START_OBJECT;
        DefaultEdge element = new DefaultEdge();
        while (parser.nextToken() != END_OBJECT) {
            assert parser.getCurrentToken() == FIELD_NAME;
            String name = parser.getCurrentName();
            parser.nextToken();
            if ("id".equals(name)) {
                assert parser.getCurrentToken() == START_OBJECT;
                element = readId(element, parser);
            } else if ("label".equals(name)) {
                assert parser.getCurrentToken() == VALUE_STRING;
                element = readLabel(element, parser);
            } else if ("type".equals(name)) {
                assert parser.getCurrentToken() == VALUE_STRING;
                element = readType(element, parser);
            } else if ("inV".equals(name)) {
                assert parser.getCurrentToken() == START_OBJECT;
                element = readInV(element, parser);
            } else if ("inVLabel".equals(name)) {
                assert parser.getCurrentToken() == VALUE_STRING;
                element = readInVLabel(element, parser);
            } else if ("outV".equals(name)) {
                assert parser.getCurrentToken() == START_OBJECT;
                element = readOutV(element, parser);
            } else if ("outVLabel".equals(name)) {
                assert parser.getCurrentToken() == VALUE_STRING;
                element = readOutVLabel(element, parser);
            } else if ("properties".equals(name)) {
                assert parser.getCurrentToken() == START_OBJECT;
                while (parser.nextToken() != END_OBJECT) {
                    assert parser.getCurrentToken() == FIELD_NAME;
                    String key = parser.getCurrentName();
                    parser.nextToken();
                    element = readProperty(element, key, parser);
                }
            } else {
                parser.skipChildren();
            }
        }
        return element;
    }

    private DefaultEdge readId(DefaultEdge edge, JsonParser parser) throws IOException {
        JsonNode jacksonNode = parser.readValueAsTree();
        edge.id = new DefaultGraphNode(jacksonNode, (ObjectMapper) parser.getCodec());
        return edge;
    }

    private DefaultEdge readLabel(DefaultEdge edge, JsonParser parser) throws IOException {
        edge.label = parser.readValueAs(String.class);
        return edge;
    }

    private DefaultEdge readType(DefaultEdge edge, JsonParser parser) throws IOException {
        String type = parser.readValueAs(String.class);
        if (type == null || !type.equals("edge"))
            throw new JsonParseException(parser, String.format("Expected 'edge' type, got '%s'", type));
        return edge;
    }

    private DefaultEdge readProperty(DefaultEdge edge, String propertyName, JsonParser parser) throws IOException {
        if (edge.properties == null) {
            edge.properties = MultimapBuilder
                    .linkedHashKeys()
                    .linkedHashSetValues()
                    .build();
        }
        JsonNode jacksonNode = parser.readValueAsTree();
        PropertyGraphNode propertyValue = new PropertyGraphNode(jacksonNode, (ObjectMapper) parser.getCodec(), propertyName, edge);
        edge.properties.put(propertyName, propertyValue);
        return edge;
    }

    private DefaultEdge readInV(DefaultEdge edge, JsonParser parser) throws IOException {
        JsonNode jacksonNode = parser.readValueAsTree();
        edge.inV = new DefaultGraphNode(jacksonNode, (ObjectMapper) parser.getCodec());
        return edge;
    }

    private DefaultEdge readInVLabel(DefaultEdge edge, JsonParser parser) throws IOException {
        edge.inVLabel = parser.readValueAs(String.class);
        return edge;
    }

    private DefaultEdge readOutV(DefaultEdge edge, JsonParser parser) throws IOException {
        JsonNode jacksonNode = parser.readValueAsTree();
        edge.outV = new DefaultGraphNode(jacksonNode, (ObjectMapper) parser.getCodec());
        return edge;
    }

    private DefaultEdge readOutVLabel(DefaultEdge edge, JsonParser parser) throws IOException {
        edge.outVLabel = parser.readValueAs(String.class);
        return edge;
    }
}
