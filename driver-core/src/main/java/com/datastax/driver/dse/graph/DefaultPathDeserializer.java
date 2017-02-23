/*
 *      Copyright (C) 2012-2017 DataStax Inc.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.fasterxml.jackson.core.JsonToken.*;

class DefaultPathDeserializer extends StdDeserializer<DefaultPath> {

    DefaultPathDeserializer() {
        super(DefaultPath.class);
    }

    @Override
    public DefaultPath deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        assert parser.getCurrentToken() == START_OBJECT;
        DefaultPath path = new DefaultPath();
        while (parser.nextToken() != END_OBJECT) {
            assert parser.getCurrentToken() == FIELD_NAME;
            String name = parser.getCurrentName();
            parser.nextToken();
            if ("labels".equals(name)) {
                while (parser.nextToken() != END_ARRAY) {
                    assert parser.getCurrentToken() == START_ARRAY;
                    Set<String> group = new LinkedHashSet<String>();
                    while (parser.nextToken() != END_ARRAY) {
                        assert parser.getCurrentToken() == VALUE_STRING;
                        String label = parser.readValueAs(String.class);
                        group.add(label);
                    }
                    if (path.labels == null)
                        path.labels = new ArrayList<Set<String>>();
                    path.labels.add(group);
                }
            } else if ("objects".equals(name)) {
                assert parser.getCurrentToken() == START_ARRAY;
                while (parser.nextToken() != END_ARRAY) {
                    assert parser.getCurrentToken() == START_OBJECT;
                    JsonNode jacksonNode = parser.readValueAsTree();
                    if (path.objects == null)
                        path.objects = new ArrayList<GraphNode>();
                    path.objects.add(new DefaultGraphNode(jacksonNode, (ObjectMapper) parser.getCodec()));
                }
            } else {
                parser.skipChildren();
            }
        }
        return path;
    }

}
