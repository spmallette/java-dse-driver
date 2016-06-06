/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.assertj.core.api.iterable.Extractor;

/**
 *
 */
class TinkerGraphExtractors {

    static <T> Extractor<VertexProperty<T>, T> vertexPropertyValue() {
        return new Extractor<VertexProperty<T>, T>() {
            @Override
            public T extract(VertexProperty<T> input) {
                return input.value();
            }
        };
    }

}
