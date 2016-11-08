/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;


import com.datastax.driver.dse.geometry.Geometry;
import com.datastax.driver.dse.geometry.LineString;
import com.datastax.driver.dse.geometry.Point;
import com.datastax.driver.dse.geometry.Polygon;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

/**
 * The default Jackson module used by DSE Graph.
 */
class DefaultGraphModule extends SimpleModule {

    DefaultGraphModule(String name, Version version) {
        super(name, version, createDeserializers(), createSerializers());
    }

    private static Map<Class<?>, JsonDeserializer<?>> createDeserializers() {

        DefaultEdgeDeserializer edgeDeserializer = new DefaultEdgeDeserializer();
        DefaultVertexDeserializer vertexDeserializer = new DefaultVertexDeserializer();
        DefaultPathDeserializer pathDeserializer = new DefaultPathDeserializer();
        DefaultPropertyDeserializer propertyDeserializer = new DefaultPropertyDeserializer();
        DefaultVertexPropertyDeserializer vertexPropertyDeserializer = new DefaultVertexPropertyDeserializer();

        return ImmutableMap.<Class<?>, JsonDeserializer<?>>builder()

                .put(Edge.class, edgeDeserializer)
                .put(Vertex.class, vertexDeserializer)
                .put(Path.class, pathDeserializer)
                .put(Property.class, propertyDeserializer)
                .put(VertexProperty.class, vertexPropertyDeserializer)

                // Inet (there is no built-in deserializer for InetAddress and subclasses)
                .put(InetAddress.class, new DefaultInetAddressDeserializer<InetAddress>(InetAddress.class))
                .put(Inet4Address.class, new DefaultInetAddressDeserializer<Inet4Address>(Inet4Address.class))
                .put(Inet6Address.class, new DefaultInetAddressDeserializer<Inet6Address>(Inet6Address.class))

                // Geospatial types
                .put(Geometry.class, new DefaultGeometryDeserializer<Geometry>(Geometry.class))
                .put(Point.class, new DefaultGeometryDeserializer<Point>(Point.class))
                .put(LineString.class, new DefaultGeometryDeserializer<LineString>(LineString.class))
                .put(Polygon.class, new DefaultGeometryDeserializer<Polygon>(Polygon.class))

                .build();
    }

    private static List<JsonSerializer<?>> createSerializers() {
        return ImmutableList.<JsonSerializer<?>>builder()
                .add(new DefaultGraphNodeSerializer())
                .add(new DefaultElementSerializer())
                .add(new DefaultGeometrySerializer())
                .build();
    }

}
