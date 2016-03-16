/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Map;

/**
 * A Jackson module that contains required deserializers to handle
 * classes in Tinkerpop's gremlin-core API.
 * <p>
 * Note: to use functionality in this package, you must run your application with JDK 8.
 */
class TinkerGraphModule extends SimpleModule {

    TinkerGraphModule(String name, Version version) {
        super(name, version, createDeserializers(), createSerializers());
    }

    private static List<JsonSerializer<?>> createSerializers() {
        return ImmutableList.<JsonSerializer<?>>builder()

                .add(new TinkerElementSerializer())

                .build();
    }

    private static Map<Class<?>, JsonDeserializer<?>> createDeserializers() {

        return ImmutableMap.<Class<?>, JsonDeserializer<?>>builder()

                .put(Edge.class, new TinkerEdgeDeserializer())
                .put(Vertex.class, new TinkerVertexDeserializer())
                .put(Path.class, new TinkerPathDeserializer())

                .build();
    }

}
