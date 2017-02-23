/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Map;

/**
 * Base class for creating deserializers which parses JSON to a {@code Map} to more easily reconstruct an object.
 */
abstract class AbstractObjectDeserializer<T> extends StdDeserializer<T> {

    protected AbstractObjectDeserializer(final Class<T> clazz) {
        super(clazz);
    }

    @Override
    public T deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        jsonParser.nextToken();

        // This will automatically parse all typed stuff.
        @SuppressWarnings("unchecked")
        final Map<String, Object> mapData = deserializationContext.readValue(jsonParser, Map.class);

        return createObject(mapData);
    }

    public abstract T createObject(final Map<String, Object> data);
}