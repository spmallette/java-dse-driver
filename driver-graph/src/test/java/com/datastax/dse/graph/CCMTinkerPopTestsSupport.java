/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph;

import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.CCMGraphTestsSupport;
import com.datastax.driver.dse.graph.GraphFixtures;
import com.datastax.driver.dse.graph.GraphOptions;
import com.datastax.dse.graph.api.DseGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

public abstract class CCMTinkerPopTestsSupport extends CCMGraphTestsSupport {

    private final boolean remote;
    protected GraphTraversalSource g;

    /**
     * Defines test with either a remote-connected traversal source
     * (using {@link DseGraph#traversal(DseSession, GraphOptions)}) or an empty
     * traversal source (using {@link DseGraph#traversal()}.
     *
     * @param remote Whether or not to use a remote traversal source.
     */
    protected CCMTinkerPopTestsSupport(boolean remote) {
        this.remote = remote;
    }

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        if (remote) {
            g = DseGraph.traversal(session());
        } else {
            g = DseGraph.traversal();
        }

        executeGraph(GraphFixtures.modern);
    }
}
