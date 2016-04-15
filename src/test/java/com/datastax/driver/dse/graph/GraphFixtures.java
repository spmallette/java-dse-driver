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

    public static final String makeStrict =
            "schema.config().option('graph.schema_mode').set(com.datastax.bdp.graph.api.model.Schema.Mode.Production)";

    /**
     * A single statement that builds the
     * <a href="http://tinkerpop.apache.org/docs/3.1.0-incubating/#intro">TinkerPop Modern</a> example graph.
     */
    public static final Collection<String> modern = Lists.newArrayList(
            makeStrict,
                    "schema.propertyKey('name').Text().ifNotExists().create();\n" +
                    "schema.propertyKey('age').Int().ifNotExists().create();\n" +
                    "schema.propertyKey('lang').Text().ifNotExists().create();\n" +
                    "schema.propertyKey('weight').Float().ifNotExists().create();\n" +
                    "schema.vertexLabel('person').properties('name', 'age').ifNotExists().create();\n" +
                    "schema.vertexLabel('software').properties('name', 'lang').ifNotExists().create();\n" +
                    "schema.edgeLabel('created').properties('weight').connection('person', 'software').ifNotExists().create();\n" +
                    "schema.edgeLabel('knows').properties('weight').connection('person', 'person').ifNotExists().create();",
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
