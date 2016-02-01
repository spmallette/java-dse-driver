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

public class Assertions extends com.datastax.driver.core.Assertions {

    public static EdgeAssert assertThat(Edge edge) {
        return new EdgeAssert(edge);
    }

    public static VertexAssert assertThat(Vertex vertex) {
        return new VertexAssert(vertex);
    }

    public static GraphResultAssert assertThat(GraphResult result) {
        return new GraphResultAssert(result);
    }

    public static PathAssert assertThat(Path path) {
        return new PathAssert(path);
    }
}
