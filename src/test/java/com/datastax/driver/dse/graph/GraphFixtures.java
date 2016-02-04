/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.google.common.collect.Lists;

import java.util.Collection;

/**
 * A group of fixtures that may be useful in multiple tests.
 */
public class GraphFixtures {

    /**
     * @return A single statement that builds the
     * <a href="http://tinkerpop.apache.org/docs/3.1.0-incubating/#intro">TinkerPop Modern</a> example graph.
     */
    public static Collection<String> modern = Lists.newArrayList(
            "graph.schema().buildVertexLabel('person').add()",
            "graph.schema().buildVertexLabel('software').add()",
            "graph.schema().buildEdgeLabel('knows').add()",
            "graph.schema().buildEdgeLabel('created').add()",
            "graph.schema().buildPropertyKey('name', String.class).add()",
            "graph.schema().buildPropertyKey('age', Integer.class).add()",
            "graph.schema().buildPropertyKey('lang', String.class).add()",
            "graph.schema().buildPropertyKey('weight', Float.class).add()",
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
