/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

/**
 * The representation of a vertex property in DSE Graph.
 * <p>
 * Vertex properties are special because they are also elements,
 * and thus have an {@link #getId() identifier}; they can also
 * contain {@link #getProperties() properties} of their own
 * (usually referred to as "meta properties").
 */
public interface VertexProperty extends Property, Element {

}
