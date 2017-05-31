/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.examples.dse.graph.core;

import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.GraphNode;
import com.datastax.driver.dse.graph.GraphResultSet;
import com.google.common.base.Joiner;

/**
 * Demonstrates a simple graph traversal, using the core driver's string-based API.
 * <p/>
 * Preconditions:
 * - a DSE cluster is running and accessible through the contacts points identified by CONTACT_POINTS and PORT.
 * - the cluster is configured for a graph workload.
 * <p/>
 * Side effects:
 * - a graph named 'demo' will be created if it does not already exist, and populated with sample data.
 *   If the graph already exists, the data is only inserted if it is not already present (we test for the
 *   existence of a vertex named 'marko').
 *
 * @see com.datastax.driver.examples.dse.graph.tinkerpop.SimpleTraversal
 */
public class SimpleTraversal {

    static String[] CONTACT_POINTS = {"127.0.0.1"};
    static int PORT = 9042;

    /**
     * Builds the
     * <a href="http://tinkerpop.apache.org/docs/3.1.0-incubating/#intro">TinkerPop Modern</a>
     * example graph.
     */
    static String MODERN_GRAPH = Joiner.on('\n').join(
            "schema.config().option('graph.schema_mode').set('production')",
            "schema.config().option('graph.allow_scan').set('true')",
            "schema.propertyKey('name').Text().ifNotExists().create();",
            "schema.propertyKey('age').Int().ifNotExists().create();",
            "schema.propertyKey('lang').Text().ifNotExists().create();",
            "schema.propertyKey('weight').Float().ifNotExists().create();",
            "schema.vertexLabel('person').properties('name', 'age').ifNotExists().create();",
            "schema.vertexLabel('software').properties('name', 'lang').ifNotExists().create();",
            "schema.edgeLabel('created').properties('weight').connection('person', 'software').ifNotExists().create();",
            "schema.edgeLabel('knows').properties('weight').connection('person', 'person').ifNotExists().create();",
            "Vertex marko = graph.addVertex(label, 'person', 'name', 'marko', 'age', 29);",
            "Vertex vadas = graph.addVertex(label, 'person', 'name', 'vadas', 'age', 27);",
            "Vertex lop = graph.addVertex(label, 'software', 'name', 'lop', 'lang', 'java');",
            "Vertex josh = graph.addVertex(label, 'person', 'name', 'josh', 'age', 32);",
            "Vertex ripple = graph.addVertex(label, 'software', 'name', 'ripple', 'lang', 'java');",
            "Vertex peter = graph.addVertex(label, 'person', 'name', 'peter', 'age', 35);",
            "marko.addEdge('knows', vadas, 'weight', 0.5f);",
            "marko.addEdge('knows', josh, 'weight', 1.0f);",
            "marko.addEdge('created', lop, 'weight', 0.4f);",
            "josh.addEdge('created', ripple, 'weight', 1.0f);",
            "josh.addEdge('created', lop, 'weight', 0.4f);",
            "peter.addEdge('created', lop, 'weight', 0.2f);");

    public static void main(String[] args) {
        DseCluster cluster = null;
        try {
            cluster = DseCluster.builder()
                    .addContactPoints(CONTACT_POINTS)
                    .withPort(PORT)
                    .build();
            DseSession session = cluster.connect();

            session.executeGraph("system.graph('demo').ifNotExists().create()");

            // Set the graph name globally so that we don't have to repeat it for each statement
            cluster.getConfiguration().getGraphOptions().setGraphName("demo");

            // Check if the graph is already populated, to avoid inserting the data multiple times
            if (session.executeGraph("g.V().has('name','marko')").one() == null) {
                session.executeGraph(MODERN_GRAPH);
            }

            // Run a simple traversal, expressed as a string:
            String traversal = "g.V().has('name','marko').out('knows').values('name')";
            GraphResultSet rs = session.executeGraph(traversal);
            for (GraphNode node : rs) {
                System.out.println(node);
            }
        } finally {
            if (cluster != null) cluster.close();
        }
    }

}
