/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph;

import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.assertj.core.api.iterable.Extractor;

public class TinkerGraphExtractors {

    public static <T> Extractor<VertexProperty<T>, T> vertexPropertyValue() {
        return Property::value;
    }

    public static <T> Extractor<Vertex, T> vertexPropertyValue(String key) {
        return vertex -> vertex.<T>property(key).value();
    }

}
