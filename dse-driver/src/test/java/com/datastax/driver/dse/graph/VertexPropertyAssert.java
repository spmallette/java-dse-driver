/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import static com.datastax.driver.dse.graph.GraphAssertions.assertThat;

public class VertexPropertyAssert extends ElementAssert<VertexPropertyAssert, VertexProperty> {

    public VertexPropertyAssert(VertexProperty actual) {
        super(actual, VertexPropertyAssert.class);
    }

    public VertexPropertyAssert hasKey(String key) {
        assertThat(actual.getName()).isEqualTo(key);
        return this;
    }

    public VertexPropertyAssert hasValue(GraphNode value) {
        assertThat(actual.getValue()).isEqualTo(value);
        return this;
    }

    public VertexPropertyAssert hasParent(Element parent) {
        assertThat(actual.getParent()).isEqualTo(parent);
        return this;
    }

    public VertexPropertyAssert hasValue(String value) {
        assertThat(actual.getValue().asString()).isEqualTo(value);
        return this;
    }
}
