/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

class GraphSON2DriverObjectsModule extends SimpleModule {

    GraphSON2DriverObjectsModule() {
        super("graph-graphson2driverobjects");
        addSerializer(Element.class, new ElementGraphSON2Serializer());
        addSerializer(ObjectGraphNode.class, new ObjectGraphNodeGraphSON2Serializer());
    }


    static final class ElementGraphSON2Serializer extends StdSerializer<Element> {

        ElementGraphSON2Serializer() {
            super(Element.class);
        }

        @Override
        public void serialize(Element element, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeObject((ObjectGraphNode) element.getId());
        }

        @Override
        public void serializeWithType(Element element, JsonGenerator jsonGenerator, SerializerProvider serializerProvider, TypeSerializer typeSerializer) throws IOException {
            serialize(element, jsonGenerator, serializerProvider);
        }
    }

    static final class ObjectGraphNodeGraphSON2Serializer extends StdSerializer<ObjectGraphNode> {

        ObjectGraphNodeGraphSON2Serializer() {
            super(ObjectGraphNode.class);
        }

        @Override
        public void serialize(ObjectGraphNode objectGraphNode, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeObject(objectGraphNode.as(Object.class));
        }

        @Override
        public void serializeWithType(ObjectGraphNode objectGraphNode, JsonGenerator jsonGenerator, SerializerProvider serializerProvider, TypeSerializer typeSerializer) throws IOException {
            serialize(objectGraphNode, jsonGenerator, serializerProvider);
        }

    }

}
