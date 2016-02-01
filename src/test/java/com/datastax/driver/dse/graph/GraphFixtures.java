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

import java.util.Collection;
import java.util.Collections;

/**
 * A group of fixtures that may be useful in multiple tests.
 */
public class GraphFixtures {

    /**
     * @return A single statement that builds the
     * <a href="http://tinkerpop.apache.org/docs/3.1.0-incubating/#intro">TinkerPop Modern</a> example graph.
     */
    public static Collection<String> modern = Collections.singletonList(
            "Vertex marko = graph.addVertex(label, 'person', 'name', 'marko', 'age', 29);\n" +
                    "Vertex vadas = graph.addVertex(label, 'person', 'name', 'vadas', 'age', 27);\n" +
                    "Vertex lop = graph.addVertex(label, 'software', 'name', 'lop', 'lang', 'java');\n" +
                    "Vertex josh = graph.addVertex(label, 'person', 'name', 'josh', 'age', 32);\n" +
                    "Vertex ripple = graph.addVertex(label, 'software', 'name', 'ripple', 'lang', 'java');\n" +
                    "Vertex peter = graph.addVertex(label, 'person', 'name', 'peter', 'age', 35);\n" +
                    "marko.addEdge('knows', vadas, 'weight', 0.5f);\n" +
                    "marko.addEdge('knows', josh, 'weight', 1.0f);\n" +
                    "marko.addEdge('created', lop, 'weight', 0.4f);\n" +
                    "josh.addEdge('created', ripple, 'weight', 1.0f);\n" +
                    "josh.addEdge('created', lop, 'weight', 0.4f);\n" +
                    "peter.addEdge('created', lop, 'weight', 0.2f);");
}
