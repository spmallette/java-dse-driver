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
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.collect.MultimapBuilder;

import java.io.IOException;

import static com.fasterxml.jackson.core.JsonToken.*;

class TinkerEdgeDeserializer extends StdDeserializer<TinkerEdge> {

    TinkerEdgeDeserializer() {
        super(TinkerEdge.class);
    }

    @Override
    public TinkerEdge deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        assert parser.getCurrentToken() == START_OBJECT;
        TinkerEdge edge = new TinkerEdge();
        while (parser.nextToken() != END_OBJECT) {
            assert parser.getCurrentToken() == FIELD_NAME;
            String name = parser.getCurrentName();
            parser.nextToken();
            if ("id".equals(name)) {
                assert parser.getCurrentToken() == START_OBJECT;
                edge = readId(edge, parser);
            } else if ("label".equals(name)) {
                assert parser.getCurrentToken() == VALUE_STRING;
                edge = readLabel(edge, parser);
            } else if ("type".equals(name)) {
                assert parser.getCurrentToken() == VALUE_STRING;
                edge = readType(edge, parser);
            } else if ("inV".equals(name)) {
                assert parser.getCurrentToken() == START_OBJECT;
                edge = readInV(edge, parser);
            } else if ("inVLabel".equals(name)) {
                assert parser.getCurrentToken() == VALUE_STRING;
                edge = readInVLabel(edge, parser);
            } else if ("outV".equals(name)) {
                assert parser.getCurrentToken() == START_OBJECT;
                edge = readOutV(edge, parser);
            } else if ("outVLabel".equals(name)) {
                assert parser.getCurrentToken() == VALUE_STRING;
                edge = readOutVLabel(edge, parser);
            } else if ("properties".equals(name)) {
                assert parser.getCurrentToken() == START_OBJECT;
                while (parser.nextToken() != END_OBJECT) {
                    assert parser.getCurrentToken() == FIELD_NAME;
                    String key = parser.getCurrentName();
                    parser.nextToken();
                    edge = readProperty(edge, key, parser);
                }
            } else {
                parser.skipChildren();
            }
        }
        return edge;
    }

    private TinkerEdge readId(TinkerEdge edge, JsonParser parser) throws IOException {
        edge.id = parser.readValueAs(Object.class);
        return edge;
    }

    private TinkerEdge readLabel(TinkerEdge edge, JsonParser parser) throws IOException {
        edge.label = parser.readValueAs(String.class);
        return edge;
    }

    private TinkerEdge readType(TinkerEdge edge, JsonParser parser) throws IOException {
        JsonLocation currentLocation = parser.getCurrentLocation();
        String type = parser.readValueAs(String.class);
        if (type == null || !type.equals("edge"))
            throw new JsonParseException(String.format("Expected 'edge' type, got '%s'", type), currentLocation);
        return edge;
    }

    private TinkerEdge readProperty(TinkerEdge edge, String propertyName, JsonParser parser) throws IOException {
        if (edge.properties == null) {
            edge.properties = MultimapBuilder
                    .linkedHashKeys()
                    .linkedHashSetValues()
                    .build();
        }
        TinkerProperty<Object> property = new TinkerProperty<Object>();
        property.key = propertyName;
        property.value = parser.readValueAs(Object.class);
        property.parent = edge;
        edge.properties.put(propertyName, property);
        return edge;
    }

    private TinkerEdge readInV(TinkerEdge edge, JsonParser parser) throws IOException {
        if (edge.inVertex == null)
            edge.inVertex = new TinkerVertex();
        edge.inVertex.id = parser.readValueAs(Object.class);
        return edge;
    }

    private TinkerEdge readInVLabel(TinkerEdge edge, JsonParser parser) throws IOException {
        if (edge.inVertex == null)
            edge.inVertex = new TinkerVertex();
        edge.inVertex.label = parser.readValueAs(String.class);
        return edge;
    }

    private TinkerEdge readOutV(TinkerEdge edge, JsonParser parser) throws IOException {
        if (edge.outVertex == null)
            edge.outVertex = new TinkerVertex();
        edge.outVertex.id = parser.readValueAs(Object.class);
        return edge;
    }

    private TinkerEdge readOutVLabel(TinkerEdge edge, JsonParser parser) throws IOException {
        if (edge.outVertex == null)
            edge.outVertex = new TinkerVertex();
        edge.outVertex.label = parser.readValueAs(String.class);
        return edge;
    }

}
