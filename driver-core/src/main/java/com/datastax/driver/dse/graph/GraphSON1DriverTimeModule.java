/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.LocalDate;
import com.datastax.driver.core.ParseUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

class GraphSON1DriverTimeModule extends SimpleModule {

    GraphSON1DriverTimeModule(String name, Version version) {
        super(name, version, createDeserializers(), createSerializers());
    }

    private static Map<Class<?>, JsonDeserializer<?>> createDeserializers() {

        return ImmutableMap.<Class<?>, JsonDeserializer<?>>builder()
                .put(LocalDate.class, new LocalDateJacksonDeserializer())
                .build();
    }

    private static List<JsonSerializer<?>> createSerializers() {
        return ImmutableList.<JsonSerializer<?>>builder()
                .add(new LocalDateJacksonSerializer())
                .build();
    }

    final static class LocalDateJacksonSerializer extends StdSerializer<LocalDate> {

        LocalDateJacksonSerializer() {
            super(LocalDate.class);
        }

        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.toString());
        }
    }

    final static class LocalDateJacksonDeserializer extends StdDeserializer<LocalDate> {

        LocalDateJacksonDeserializer() {
            super(LocalDate.class);
        }

        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String dateString = p.getText();
            try {
                Date date = ParseUtils.parseDate(dateString);
                return LocalDate.fromMillisSinceEpoch(date.getTime());
            } catch (ParseException e) {
                throw new IOException("Could not deserialize data as a LocalDate.", e);
            }
        }
    }

}
