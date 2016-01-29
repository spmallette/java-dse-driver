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
package com.datastax.driver.graph;

import org.assertj.core.api.AbstractAssert;

import static com.datastax.driver.graph.Assertions.assertThat;

public class GraphResultAssert extends AbstractAssert<GraphResultAssert, GraphResult> {
    protected GraphResultAssert(GraphResult actual) {
        super(actual, GraphResultAssert.class);
    }

    public EdgeAssert asEdge() {
        return new EdgeAssert(actual.asEdge());
    }

    public VertexAssert asVertex() {
        return new VertexAssert(actual.asVertex());
    }

    public GraphResultAssert hasChild(String key) {
        assertThat(actual.get(key).isNull()).isFalse();
        return myself;
    }

    public GraphResultAssert hasChild(int index) {
        assertThat(actual.get(index).isNull()).isFalse();
        return myself;
    }

    public GraphResultAssert child(String key) {
        hasChild(key);
        return new GraphResultAssert(actual.get(key));
    }

    public GraphResultAssert child(int index) {
        hasChild(index);
        return new GraphResultAssert(actual.get(index));
    }
}
