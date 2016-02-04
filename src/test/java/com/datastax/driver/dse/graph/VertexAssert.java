/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

public class VertexAssert extends ElementAssert<VertexAssert, Vertex> {
    protected VertexAssert(Vertex actual) {
        super(actual, VertexAssert.class);
    }
}
