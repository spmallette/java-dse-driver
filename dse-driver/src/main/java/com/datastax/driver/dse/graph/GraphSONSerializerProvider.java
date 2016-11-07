/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * Implementation of the {@link DefaultSerializerProvider} for Jackson that uses the {@link ToStringSerializer} for
 * unknown types.
 */
final class GraphSONSerializerProvider extends DefaultSerializerProvider {
    private static final long serialVersionUID = 1L;
    private final JsonSerializer<Object> unknownTypeSerializer;

    public GraphSONSerializerProvider() {
        super();
        //TODO
//        setDefaultKeySerializer(new GraphSONSerializersV1d0.GraphSONKeySerializer());
        unknownTypeSerializer = new ToStringGraphSON2Serializer();
    }

    protected GraphSONSerializerProvider(final SerializerProvider src,
                                         final SerializationConfig config, final SerializerFactory f,
                                         final JsonSerializer<Object> unknownTypeSerializer) {
        super(src, config, f);
        this.unknownTypeSerializer = unknownTypeSerializer;
    }

    @Override
    public JsonSerializer<Object> getUnknownTypeSerializer(final Class<?> aClass) {
        return unknownTypeSerializer;
    }

    @Override
    public GraphSONSerializerProvider createInstance(final SerializationConfig config,
                                                     final SerializerFactory jsf) {
        // createInstance is called pretty often to create a new SerializerProvider
        // we give it the unknownTypeSerializer that we had in the first place,
        // when the object was first constructed through the public constructor
        // that has a GraphSONVersion.
        return new GraphSONSerializerProvider(this, config, jsf, unknownTypeSerializer);
    }
}
