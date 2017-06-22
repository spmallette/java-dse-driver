/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class TinkerElementAssert<S extends AbstractAssert<S, A>, A extends Element> extends
        AbstractAssert<S, A> {

    protected TinkerElementAssert(A actual, Class<?> selfType) {
        super(actual, selfType);
    }

    public S hasId(Object id) {
        assertThat(actual.id()).isEqualTo(id);
        return myself;
    }

    public S hasLabel(String label) {
        assertThat(actual.label()).isEqualTo(label);
        return myself;
    }

    public S hasProperty(String propertyName) {
        assertThat(actual.property(propertyName).isPresent()).isTrue();
        return myself;
    }

    public S hasProperty(String propertyName, Object value) {
        hasProperty(propertyName);
        assertThat(actual.property(propertyName).value()).isEqualTo(value);
        return myself;
    }

}