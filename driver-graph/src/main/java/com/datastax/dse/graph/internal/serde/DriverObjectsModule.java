/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph.internal.serde;

import com.datastax.driver.dse.graph.Element;
import com.datastax.driver.dse.graph.ObjectGraphNode;
import org.apache.tinkerpop.shaded.jackson.core.JsonGenerator;
import org.apache.tinkerpop.shaded.jackson.databind.SerializerProvider;
import org.apache.tinkerpop.shaded.jackson.databind.jsontype.TypeSerializer;
import org.apache.tinkerpop.shaded.jackson.databind.module.SimpleModule;
import org.apache.tinkerpop.shaded.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class DriverObjectsModule extends SimpleModule {

    public DriverObjectsModule() {
        super("datastax-driver-module");
        addSerializer(Element.class, new ElementGraphSON2Serializer());
        addSerializer(ObjectGraphNode.class, new ObjectGraphNodeGraphSON2Serializer());
    }

    static final class ElementGraphSON2Serializer extends StdSerializer<Element> {

        protected ElementGraphSON2Serializer() {
            super(Element.class);
        }

        @Override
        public void serialize(Element element, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeObject(element.getId());
        }

        @Override
        public void serializeWithType(Element element, JsonGenerator jsonGenerator, SerializerProvider serializerProvider, TypeSerializer typeSerializer) throws IOException {
            serialize(element, jsonGenerator, serializerProvider);
        }
    }

    static final class ObjectGraphNodeGraphSON2Serializer extends StdSerializer<ObjectGraphNode> {

        protected ObjectGraphNodeGraphSON2Serializer() {
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
