/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;

import java.util.Map;

/**
 * A default representation of a vertex in DSE graph.
 */
public class Vertex extends Element {

    public Vertex(GraphResult id, String label, String type, Map<String, GraphResult> properties) {
        super(id, label, type, properties);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other instanceof Vertex) {
            Vertex that = (Vertex) other;
            return Objects.equal(this.id, that.id) &&
                    Objects.equal(this.label, that.label) &&
                    Objects.equal(this.type, that.type) &&
                    Objects.equal(this.properties, that.properties);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, label, type, properties);
    }

    @Override
    public String toString() {
        return "Graph Vertex [" +
                String.format("id = %s, label = %s", this.id, this.label) +
                ", properties = {" +
                (this.properties == null ? "" : Joiner.on(", ").withKeyValueSeparator(":").join(this.properties)) +
                "}]";
    }
}
