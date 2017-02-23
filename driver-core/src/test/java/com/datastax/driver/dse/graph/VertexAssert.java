/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import static org.assertj.core.api.Assertions.assertThat;

public class VertexAssert extends ElementAssert<VertexAssert, Vertex> {

    public VertexAssert(Vertex actual) {
        super(actual, VertexAssert.class);
    }

    @Override
    public VertexAssert hasProperty(String propertyName) {
        assertThat(actual.getPropertyNames()).contains(propertyName);
        return myself;
    }

    @Override
    public VertexAssert hasProperty(String propertyName, String value) {
        return hasProperty(propertyName, value, String.class);
    }

    @Override
    public VertexAssert hasProperty(String propertyName, int value) {
        return hasProperty(propertyName, value, Integer.class);
    }

    @Override
    public VertexAssert hasProperty(String propertyName, Boolean value) {
        return hasProperty(propertyName, value, Boolean.class);
    }

    @Override
    public VertexAssert hasProperty(String propertyName, Long value) {
        return hasProperty(propertyName, value, Long.class);
    }

    @Override
    public VertexAssert hasProperty(String propertyName, Double value) {
        return hasProperty(propertyName, value, Double.class);
    }

    @Override
    public VertexAssert hasProperty(String propertyName, Float value) {
        return hasProperty(propertyName, value, Float.class);
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
