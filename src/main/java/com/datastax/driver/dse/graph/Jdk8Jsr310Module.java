/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.dse.IgnoreJDK6Requirement;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
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
import java.util.List;
import java.util.Map;

/**
 * Serializers and deserializers for JSR 310 {@code java.time.*}.
 */
@IgnoreJDK6Requirement
@SuppressWarnings("Since15")
class Jdk8Jsr310Module extends SimpleModule {

    Jdk8Jsr310Module(String name, Version version) {
        super(name, version, createDeserializers(), createSerializers());
    }

    private static Map<Class<?>, JsonDeserializer<?>> createDeserializers() {

        return ImmutableMap.<Class<?>, JsonDeserializer<?>>builder()
                .put(java.time.Duration.class, new DurationJacksonDeserializer())
                .put(java.time.Instant.class, new InstantJacksonDeserializer())
                .put(java.time.ZonedDateTime.class, new ZonedDateTimeJacksonDeserializer())
                .build();
    }

    private static List<JsonSerializer<?>> createSerializers() {
        return ImmutableList.<JsonSerializer<?>>builder()
                .add(new DurationJacksonSerializer())
                .add(new InstantJacksonSerializer())
                .add(new ZonedDateTimeJacksonSerializer())
                .build();
    }

    /**
     * Base class for serializing the {@code java.time.*} types to ISO-8061 formats.
     */
    @IgnoreJDK6Requirement
    static abstract class AbstractJavaTimeSerializer<T> extends StdSerializer<T> {

        public AbstractJavaTimeSerializer(final Class<T> clazz) {
            super(clazz);
        }

        @Override
        public void serialize(final T value, final JsonGenerator gen,
                              final SerializerProvider serializerProvider) throws IOException {
            gen.writeString(value.toString());
        }

    }

    /**
     * Base class for deserializing the {@code java.time.*} types from ISO-8061 formats.
     */
    @IgnoreJDK6Requirement
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
        public InstantJacksonDeserializer() {
            super(java.time.Instant.class);
        }

        @Override
        public java.time.Instant parse(final String val) {
            return java.time.Instant.parse(val);
        }
    }


    @IgnoreJDK6Requirement
    final static class ZonedDateTimeJacksonSerializer extends AbstractJavaTimeSerializer<java.time.ZonedDateTime> {

        public ZonedDateTimeJacksonSerializer() {
            super(java.time.ZonedDateTime.class);
        }
    }

    @IgnoreJDK6Requirement
    final static class ZonedDateTimeJacksonDeserializer extends AbstractJavaTimeJacksonDeserializer<java.time.ZonedDateTime> {
        public ZonedDateTimeJacksonDeserializer() {
            super(java.time.ZonedDateTime.class);
        }

        @Override
        public java.time.ZonedDateTime parse(final String val) {
            return java.time.ZonedDateTime.parse(val);
        }
    }
}
