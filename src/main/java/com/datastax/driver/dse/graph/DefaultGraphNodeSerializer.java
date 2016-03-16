/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Default serializer used by the driver for {@link DefaultGraphNode} instances.
 */
class DefaultGraphNodeSerializer extends StdSerializer<DefaultGraphNode> {

    DefaultGraphNodeSerializer() {
        super(DefaultGraphNode.class);
    }

    @Override
    public void serialize(DefaultGraphNode value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeTree(value.delegate);
    }

}
