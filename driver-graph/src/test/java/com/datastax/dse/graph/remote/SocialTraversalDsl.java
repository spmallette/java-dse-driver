/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph.remote;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.GremlinDsl;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

@GremlinDsl(traversalSource = "com.datastax.dse.graph.remote.SocialTraversalSourceDsl")
public interface SocialTraversalDsl<S, E> extends GraphTraversal.Admin<S, E> {
    public default GraphTraversal<S, Vertex> knows(String personName) {
        return out("knows").hasLabel("person").has("name", personName).in();
    }
}