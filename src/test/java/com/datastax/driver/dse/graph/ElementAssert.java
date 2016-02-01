/*
 *      Copyright (C) 2012-2015 DataStax Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.datastax.driver.dse.graph;

import org.assertj.core.api.AbstractAssert;

import static com.datastax.driver.core.Assertions.assertThat;

public abstract class ElementAssert<S extends AbstractAssert<S, A>, A extends Element> extends
        AbstractAssert<S, A> {

    protected ElementAssert(A actual, Class<?> selfType) {
        super(actual, selfType);
    }

    public S hasId(GraphResult id) {
        assertThat(actual.getId()).isEqualTo(id);
        return myself;
    }

    public S hasLabel(String label) {
        assertThat(actual.getLabel()).isEqualTo(label);
        return myself;
    }

    public S hasType(String type) {
        assertThat(actual.getType()).isEqualTo(type);
        return myself;
    }

    public S hasProperty(String propertyName) {
        assertThat(actual.getProperties()).containsKey(propertyName);
        return myself;
    }

    public S hasProperty(String propertyName, GraphResult value) {
        hasProperty(propertyName);
        assertThat(actual.getProperties().get(propertyName)).isEqualTo(value);
        return myself;
    }

    public S hasProperty(String propertyName, String value) {
        hasProperty(propertyName);
        GraphResult result = actual.getProperties().get(propertyName);
        assertThat(result.asString()).isEqualTo(value);
        return myself;
    }

    public S hasProperty(String propertyName, int value) {
        hasProperty(propertyName);
        GraphResult result = actual.getProperties().get(propertyName);
        assertThat(result.asInt()).isEqualTo(value);
        return myself;
    }

    public S hasProperty(String propertyName, Boolean value) {
        hasProperty(propertyName);
        GraphResult result = actual.getProperties().get(propertyName);
        assertThat(result.asBoolean()).isEqualTo(value);
        return myself;
    }

    public S hasProperty(String propertyName, Long value) {
        hasProperty(propertyName);
        GraphResult result = actual.getProperties().get(propertyName);
        assertThat(result.asLong()).isEqualTo(value);
        return myself;
    }

    public S hasProperty(String propertyName, Double value) {
        hasProperty(propertyName);
        GraphResult result = actual.getProperties().get(propertyName);
        assertThat(result.asDouble()).isEqualTo(value);
        return myself;
    }

    public S hasProperty(String propertyName, Vertex value) {
        hasProperty(propertyName);
        GraphResult result = actual.getProperties().get(propertyName);
        assertThat(result.asVertex()).isEqualTo(value);
        return myself;
    }

    public S hasProperty(String propertyName, Edge value) {
        hasProperty(propertyName);
        GraphResult result = actual.getProperties().get(propertyName);
        assertThat(result.asEdge()).isEqualTo(value);
        return myself;
    }

    public <T> S hasProperty(String propertyName, T value, Class<T> clazz) {
        hasProperty(propertyName);
        GraphResult result = actual.getProperties().get(propertyName);
        assertThat(result.as(clazz)).isEqualTo(value);
        return myself;
    }

}
