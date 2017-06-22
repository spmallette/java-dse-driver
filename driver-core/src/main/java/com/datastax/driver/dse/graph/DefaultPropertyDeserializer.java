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

import java.io.IOException;

class DefaultPropertyDeserializer extends StdDeserializer<DefaultProperty> {

    DefaultPropertyDeserializer() {
        super(DefaultProperty.class);
    }

    @Override
    public DefaultProperty deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        if (!(parser instanceof PropertyGraphNodeParser))
            throw new JsonParseException(parser, "Cannot deserialize property if parser is not instance of PropertyGraphNodeParser");
        PropertyGraphNodeParser propertyParser = (PropertyGraphNodeParser) parser;
        String name = propertyParser.propertyName;
        if (name == null)
            throw new JsonParseException(parser, "Cannot deserialize property without its name");
        Element parent = propertyParser.parent;
        if (parent == null)
            throw new JsonParseException(parser, "Cannot deserialize property with null parent");
        JsonNode jacksonNode = parser.readValueAsTree();
        DefaultProperty property = new DefaultProperty();
        property.name = name;
        property.value = new DefaultGraphNode(jacksonNode, (ObjectMapper) parser.getCodec());
        property.parent = parent;
        return property;
    }
}
