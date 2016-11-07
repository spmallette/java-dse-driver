/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import org.assertj.core.api.AbstractAssert;

import static com.datastax.driver.core.Assertions.assertThat;
import static com.datastax.driver.dse.graph.GraphExtractors.propertyValueAs;

public abstract class ElementAssert<S extends AbstractAssert<S, A>, A extends Element> extends
        AbstractAssert<S, A> {

    protected ElementAssert(A actual, Class<?> selfType) {
        super(actual, selfType);
    }

    public S hasId(GraphNode id) {
        assertThat(actual.getId()).isEqualTo(id);
        return myself;
    }

    public S hasLabel(String label) {
        assertThat(actual.getLabel()).isEqualTo(label);
        return myself;
    }

    public S hasProperty(String propertyName) {
        assertThat(actual.getPropertyNames()).contains(propertyName);
        return myself;
    }

    public S hasProperty(String propertyName, String value) {
        return hasProperty(propertyName, value, String.class);
    }

    public S hasProperty(String propertyName, int value) {
        return hasProperty(propertyName, value, Integer.class);
    }

    public S hasProperty(String propertyName, Boolean value) {
        return hasProperty(propertyName, value, Boolean.class);
    }

    public S hasProperty(String propertyName, Long value) {
        return hasProperty(propertyName, value, Long.class);
    }

    public S hasProperty(String propertyName, Double value) {
        return hasProperty(propertyName, value, Double.class);
    }

    public S hasProperty(String propertyName, Float value) {
        return hasProperty(propertyName, value, Float.class);
    }

    @SuppressWarnings("unchecked")
    public <T> S hasProperty(String propertyName, T value, Class<T> clazz) {
        hasProperty(propertyName);
        assertThat(actual.getProperties(propertyName))
                .extracting(propertyValueAs(clazz))
                .contains(value);
        return myself;
    }

}
