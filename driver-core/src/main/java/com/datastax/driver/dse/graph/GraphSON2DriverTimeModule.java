/*
 *      Copyright (C) 2012-2017 DataStax Inc.
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
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

class GraphSON2DriverTimeModule extends GraphSON2JacksonModule {

    GraphSON2DriverTimeModule() {
        super("graph-graphson2drivertime");

        addSerializer(LocalDate.class, new JavaDriverLocalDateSerializer());

        addDeserializer(LocalDate.class, new JavaDriverLocalDateDeserializer());
    }

    @Override
    public Map<Class<?>, String> getTypeDefinitions() {
        final ImmutableMap.Builder<Class<?>, String> builder = ImmutableMap.builder();
        builder.put(Date.class, "Instant");
        return builder.build();
    }

    @Override
    public String getTypeNamespace() {
        return "gx";
    }

    final static class JavaDriverLocalDateDeserializer extends StdDeserializer<LocalDate> {

        JavaDriverLocalDateDeserializer() {
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

    final static class JavaDriverLocalDateSerializer extends StdScalarSerializer<LocalDate> {

        JavaDriverLocalDateSerializer() {
            super(LocalDate.class);
        }

        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.toString());
        }
    }
}
