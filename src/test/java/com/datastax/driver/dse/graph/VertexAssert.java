/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import static org.assertj.core.api.Assertions.assertThat;

public class VertexAssert extends ElementAssert<VertexAssert, Vertex> {

    public VertexAssert(Vertex actual) {
        super(actual, VertexAssert.class);
    }

    public VertexAssert hasProperty(String propertyName) {
        assertThat(actual.getPropertyNames()).contains(propertyName);
        return myself;
    }

    public VertexAssert hasProperty(String propertyName, String value) {
        return hasProperty(propertyName, value, String.class);
    }

    public VertexAssert hasProperty(String propertyName, int value) {
        return hasProperty(propertyName, value, Integer.class);
    }

    public VertexAssert hasProperty(String propertyName, Boolean value) {
        return hasProperty(propertyName, value, Boolean.class);
    }

    public VertexAssert hasProperty(String propertyName, Long value) {
        return hasProperty(propertyName, value, Long.class);
    }

    public VertexAssert hasProperty(String propertyName, Double value) {
        return hasProperty(propertyName, value, Double.class);
    }

    @SuppressWarnings("unchecked")
    public <T> VertexAssert hasProperty(String propertyName, T value, Class<T> clazz) {
        hasProperty(propertyName);
        assertThat(actual.getProperties(propertyName))
                .extracting(GraphExtractors.vertexPropertyValueAs(clazz))
                .contains(value);
        return myself;
    }

}
