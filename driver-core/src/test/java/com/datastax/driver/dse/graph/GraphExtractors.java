/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import org.assertj.core.api.iterable.Extractor;

/**
 *
 */
public class GraphExtractors {

    public static Extractor<GraphNode, Vertex> asVertex() {
        return new Extractor<GraphNode, Vertex>() {
            @Override
            public Vertex extract(GraphNode input) {
                return input.asVertex();
            }
        };
    }

    public static Extractor<GraphNode, Edge> asEdge() {
        return new Extractor<GraphNode, Edge>() {
            @Override
            public Edge extract(GraphNode input) {
                return input.asEdge();
            }
        };
    }

    public static Extractor<GraphNode, Property> asProperty() {
        return new Extractor<GraphNode, Property>() {
            @Override
            public Property extract(GraphNode input) {
                return input.asProperty();
            }
        };
    }

    public static Extractor<GraphNode, VertexProperty> asVertexProperty() {
        return new Extractor<GraphNode, VertexProperty>() {
            @Override
            public VertexProperty extract(GraphNode input) {
                return input.asVertexProperty();
            }
        };
    }

    public static Extractor<GraphNode, Path> asPath() {
        return new Extractor<GraphNode, Path>() {
            @Override
            public Path extract(GraphNode input) {
                return input.asPath();
            }
        };
    }

    public static <T> Extractor<Property, T> propertyValueAs(final Class<T> clazz) {
        return new Extractor<Property, T>() {
            @Override
            public T extract(Property input) {
                return input.getValue().as(clazz);
            }
        };
    }

    public static <T> Extractor<Vertex, T> propertyValueAs(final String propertyName, final Class<T> clazz) {
        return new Extractor<Vertex, T>() {
            @Override
            public T extract(Vertex input) {
                return input.getProperty(propertyName).getValue().as(clazz);
            }
        };
    }

    public static <T> Extractor<VertexProperty, T> vertexPropertyValueAs(final Class<T> clazz) {
        return new Extractor<VertexProperty, T>() {
            @Override
            public T extract(VertexProperty input) {
                return input.getValue().as(clazz);
            }
        };
    }

    public static <T> Extractor<Vertex, T> vertexPropertyValueAs(final String propertyName, final Class<T> clazz) {
        return new Extractor<Vertex, T>() {
            @Override
            public T extract(Vertex input) {
                return input.getProperty(propertyName).getValue().as(clazz);
            }
        };
    }

    public static <T> Extractor<GraphNode, T> fieldAs(final String fieldName, final Class<T> clazz) {
        return new Extractor<GraphNode, T>() {
            @Override
            public T extract(GraphNode input) {
                return input.get(fieldName).as(clazz);
            }
        };
    }

    public static <T> Extractor<GraphNode, T> elementAs(final int index, final Class<T> clazz) {
        return new Extractor<GraphNode, T>() {
            @Override
            public T extract(GraphNode input) {
                return input.get(index).as(clazz);
            }
        };
    }

}
