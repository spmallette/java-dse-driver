/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph.api;

import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.GraphJsonUtils;
import com.datastax.driver.dse.graph.GraphOptions;
import com.datastax.driver.dse.graph.GraphStatement;
import com.datastax.dse.graph.internal.DseRemoteConnection;
import com.datastax.dse.graph.internal.utils.GraphSONUtils;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;

/**
 * Utility class for interacting with DataStax Enterprise Graph and Apache TinkerPop.
 */
public final class DseGraph {

    /**
     * Create a {@link org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource} that will be used
     * to build {@link org.apache.tinkerpop.gremlin.process.traversal.Traversal}s for using with the {@link #statementFromTraversal}
     * method.
     * <p/>
     * Iterating on a {@link org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource} created with this method
     * will not work.
     *
     * @return a simple and non-iterable traversal source, associated to a {@link org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph}.
     */
    public static GraphTraversalSource traversal() {
        return EmptyGraph.instance().traversal();
    }

    /**
     * Create a {@link org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource} initialized to work remotely
     * with a DSE Graph server, communicating via the DataStax Enterprise Java Driver.
     *
     * @param dseSession an initialized and active session created with a {@link com.datastax.driver.dse.DseCluster}
     *                   that will be used internally to communicate with the DSE server.
     *                   All of the configurations made on the DseSession's creation will
     *                   be effective when the traversal source is used.
     * @param graphOptions configurations to use for this traversal source. The options
     *                     on this object will override the ones defined on the {@link com.datastax.driver.dse.graph.GraphOptions}
     *                     of the {@link com.datastax.driver.dse.DseCluster} behind the DseSession
     *                     input.
     * @return a remotely connected and initialized traversal source that can be used right away.
     */
    public static GraphTraversalSource traversal(DseSession dseSession, GraphOptions graphOptions) {
        return traversal().withRemote(DseRemoteConnection.builder(dseSession)
                .withGraphOptions(graphOptions).build());
    }

    /**
     * Create a {@link org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource} initialized to work remotely
     * with a DSE Graph server, communicating via the DataStax Enterprise Java Driver.
     *
     * @param dseSession   an initialized and active session created with a {@link com.datastax.driver.dse.DseCluster}
     *                     that will be used internally to communicate with the DSE server.
     *                     All of the configurations made on the DseSession's creation will
     *                     be effective when the traversal source is used.
     * @return a remotely connected and initialized traversal source that can be used right away.
     */
    public static GraphTraversalSource traversal(DseSession dseSession) {
        return traversal().withRemote(DseRemoteConnection.builder(dseSession).build());
    }

    /**
     * Create an initialized {@link com.datastax.driver.dse.graph.GraphStatement} from a {@link org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal}
     * to use directly with a {@link com.datastax.driver.dse.DseSession}.
     *
     * @param traversal the Traversal to use to create the statement.
     * @return a statement executable in a {@link com.datastax.driver.dse.DseSession#executeGraph} or {@link com.datastax.driver.dse.DseSession#executeGraphAsync} call.
     */
    public static <S, E> GraphStatement statementFromTraversal(GraphTraversal<S, E> traversal) {
        return GraphSONUtils.getStatementFromBytecode(traversal.asAdmin().getBytecode())
                .setTransformResultFunction(GraphJsonUtils.ROW_TO_GRAPHSON2_OBJECTGRAPHNODE);
    }
}
