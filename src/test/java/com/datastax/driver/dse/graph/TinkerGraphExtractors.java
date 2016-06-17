/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
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
