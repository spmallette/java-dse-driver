/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.fasterxml.jackson.core.JsonToken.*;

class TinkerPathDeserializer extends StdDeserializer<TinkerPath> {

    private static final String TYPE = "type";
    private static final String VERTEX_TYPE = "vertex";
    private static final String EDGE_TYPE = "edge";

    TinkerPathDeserializer() {
        super(TinkerPath.class);
    }

    @Override
    public TinkerPath deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        assert parser.getCurrentToken() == START_OBJECT;
        TinkerPath path = new TinkerPath();
        while (parser.nextToken() != END_OBJECT) {
            assert parser.getCurrentToken() == FIELD_NAME;
            String name = parser.getCurrentName();
            if ("labels".equals(name)) {
                int i = 0;
                parser.nextToken();
                while (parser.nextToken() != END_ARRAY) {
                    assert parser.getCurrentToken() == START_ARRAY;
                    path = startNewLabelGroup(path, i);
                    while (parser.nextToken() != END_ARRAY) {
                        assert parser.getCurrentToken() == VALUE_STRING;
                        String label = parser.readValueAs(String.class);
                        path = addLabel(path, i, label);
                    }
                    i++;
                }
            } else if ("objects".equals(name)) {
                parser.nextToken();
                assert parser.getCurrentToken() == START_ARRAY;
                while (parser.nextToken() != END_ARRAY) {
                    assert parser.getCurrentToken() == START_OBJECT;
                    path = readObject(path, parser);
                }
            } else {
                parser.nextToken();
                parser.skipChildren();
            }
        }
        return path;
    }

    private TinkerPath startNewLabelGroup(TinkerPath path, int labelGroupIndex) {
        if (path.labels == null)
            path.labels = new ArrayList<Set<String>>();
        path.labels.add(labelGroupIndex, new LinkedHashSet<String>());
        return path;
    }

    private TinkerPath addLabel(TinkerPath path, int labelGroupIndex, String label) {
        path.labels.get(labelGroupIndex).add(label);
        return path;
    }

    private TinkerPath readObject(TinkerPath path, JsonParser parser) throws IOException {
        if (path.objects == null)
            path.objects = new ArrayList<Object>();
        JsonNode node = parser.readValueAsTree();
        ObjectMapper objectMapper = (ObjectMapper) parser.getCodec();
        if (isVertex(node))
            path.objects.add(objectMapper.treeToValue(node, Vertex.class));
        else if (isEdge(node))
            path.objects.add(objectMapper.treeToValue(node, Edge.class));
        else
            path.objects.add(objectMapper.treeToValue(node, Object.class));
        return path;
    }

    private static boolean isVertex(JsonNode node) {
        return VERTEX_TYPE.equals(node.get(TYPE).asText());
    }

    private static boolean isEdge(JsonNode node) {
        return EDGE_TYPE.equals(node.get(TYPE).asText());
    }

}
