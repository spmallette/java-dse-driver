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
