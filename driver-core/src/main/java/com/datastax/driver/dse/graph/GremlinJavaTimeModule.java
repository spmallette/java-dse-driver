/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.dse.IgnoreJDK6Requirement;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.Map;

@IgnoreJDK6Requirement
@SuppressWarnings("Since15")
class GremlinJavaTimeModule extends GraphSON2JacksonModule {

    public GremlinJavaTimeModule() {
        super("graph-graphson2time");

        addSerializer(java.time.Duration.class, new DurationJacksonSerializer());
        addSerializer(java.time.Instant.class, new InstantJacksonSerializer());

        addDeserializer(java.time.Duration.class, new DurationJacksonDeserializer());
        addDeserializer(java.time.Instant.class, new InstantJacksonDeserializer());
    }

    @Override
    public Map<Class<?>, String> getTypeDefinitions() {
        final ImmutableMap.Builder<Class<?>, String> builder = ImmutableMap.builder();

        builder.put(java.time.Instant.class, "Instant");
        builder.put(java.time.Duration.class, "Duration");

        return builder.build();
    }

    @Override
    public String getTypeNamespace() {
        return "gx";
    }

    /**
     * Base class for serializing the {@code java.time.*} from ISO-8061 formats.
     */
    abstract static class AbstractJavaTimeJacksonDeserializer<T> extends StdDeserializer<T> {
        public AbstractJavaTimeJacksonDeserializer(final Class<T> clazz) {
            super(clazz);
        }

        public abstract T parse(final String val);

        @Override
        public T deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
            return parse(jsonParser.getText());
        }
    }

    /**
     * Base class for serializing the {@code java.time.*} to ISO-8061 formats.
     */
    static abstract class AbstractJavaTimeSerializer<T> extends StdSerializer<T> {

        public AbstractJavaTimeSerializer(final Class<T> clazz) {
            super(clazz);
        }

        @Override
        public void serialize(final T value, final JsonGenerator gen,
                              final SerializerProvider serializerProvider) throws IOException {
            gen.writeString(value.toString());
        }

        @Override
        public void serializeWithType(final T value, final JsonGenerator gen,
                                      final SerializerProvider serializers, final TypeSerializer typeSer) throws IOException {
            typeSer.writeTypePrefixForScalar(value, gen);
            gen.writeString(value.toString());
            typeSer.writeTypeSuffixForScalar(value, gen);
        }
    }

    @IgnoreJDK6Requirement
    final static class DurationJacksonSerializer extends AbstractJavaTimeSerializer<java.time.Duration> {

        public DurationJacksonSerializer() {
            super(java.time.Duration.class);
        }
    }

    @IgnoreJDK6Requirement
    final static class DurationJacksonDeserializer extends AbstractJavaTimeJacksonDeserializer<java.time.Duration> {
        public DurationJacksonDeserializer() {
            super(java.time.Duration.class);
        }

        @Override
        public java.time.Duration parse(final String val) {
            return java.time.Duration.parse(val);
        }
    }

    @IgnoreJDK6Requirement
    final static class InstantJacksonSerializer extends AbstractJavaTimeSerializer<java.time.Instant> {

        public InstantJacksonSerializer() {
            super(java.time.Instant.class);
        }
    }

    @IgnoreJDK6Requirement
    final static class InstantJacksonDeserializer extends AbstractJavaTimeJacksonDeserializer<java.time.Instant> {

        InstantJacksonDeserializer() {
            super(java.time.Instant.class);
        }

        @Override
        public java.time.Instant parse(final String val) {
            return java.time.Instant.parse(val);
        }
    }


}
