/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph.internal;

import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.GraphOptions;
import com.datastax.driver.dse.graph.GraphResultSet;
import com.datastax.driver.dse.graph.GraphStatement;
import com.datastax.dse.graph.internal.utils.GraphSONUtils;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.tinkerpop.gremlin.process.remote.RemoteConnection;
import org.apache.tinkerpop.gremlin.process.remote.RemoteConnectionException;
import org.apache.tinkerpop.gremlin.process.remote.traversal.RemoteTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.Bytecode;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DseRemoteConnection implements RemoteConnection {
    private final DseSession dseSession;
    private final GraphOptions graphOptions;

    private DseRemoteConnection(Builder builder) {
        this.dseSession = builder.dseSession;
        this.graphOptions = builder.graphOptions;
    }

    /**
     * Get a {@link Builder} to create a properly initialized {@link DseRemoteConnection}.
     * <p/>
     * <p/>
     * This method must take a properly initialized {@link DseSession} that it will reuse
     * internally.
     *
     * @param dseSession the session to use to communicate with the DataStax Enterprise Graph Server.
     * @return builder class to create a {@link DseRemoteConnection}.
     */
    public static Builder builder(DseSession dseSession) {
        return new Builder(dseSession);
    }

    @Override
    @SuppressWarnings("deprecation")
    public <E> Iterator<Traverser.Admin<E>> submit(Traversal<?, E> traversal) throws RemoteConnectionException {
        return submit(traversal.asAdmin().getBytecode());
    }

    @Override
    @SuppressWarnings({"deprecation", "unchecked"})
    // Tinkerpop (3.2.2 -> 3.2.3) will use this method
    public <E> RemoteTraversal<?, E> submit(Bytecode bytecode) throws RemoteConnectionException {
        try {
            return (RemoteTraversal<?, E>) submitAsync(bytecode).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RemoteConnectionException(e);
        }
    }

    // TinkerPop 3.2.4+ will use this method
    public <E> CompletableFuture<RemoteTraversal<?, E>> submitAsync(Bytecode bytecode) throws RemoteConnectionException {
        GraphStatement graphStatement = GraphSONUtils.getStatementFromBytecode(bytecode);
        // override the transformFunction manually as we want to deserialize into TP types.
        graphStatement.setTransformResultFunction(GraphSONUtils.ROW_TO_GRAPHSON2_TINKERPOP_OBJECTGRAPHNODE);
        applyGraphOptionsOnStatement(graphStatement, this.graphOptions);
        ListenableFuture<GraphResultSet> listenableFutureResults = dseSession.executeGraphAsync(graphStatement);

        return buildCompletableFutureResultSet(listenableFutureResults).thenApply(DseRemoteTraversal::new);
    }

    private static CompletableFuture<GraphResultSet> buildCompletableFutureResultSet(final ListenableFuture<GraphResultSet> listenableFuture) {
        //create an instance of CompletableFuture
        CompletableFuture<GraphResultSet> completable = new CompletableFuture<GraphResultSet>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                // propagate cancel to the listenable future
                boolean result = listenableFuture.cancel(mayInterruptIfRunning);
                super.cancel(mayInterruptIfRunning);
                return result;
            }
        };

        // add callback
        Futures.addCallback(listenableFuture, new FutureCallback<GraphResultSet>() {
            @Override
            public void onSuccess(GraphResultSet result) {
                completable.complete(result);
            }

            @Override
            public void onFailure(Throwable t) {
                completable.completeExceptionally(t);
            }
        });
        return completable;
    }

    @Override
    public void close() throws Exception {
        // do not close the DseSession here.
    }

    private void applyGraphOptionsOnStatement(GraphStatement graphStatement, GraphOptions graphOptions) {
        if (graphOptions == null) {
            return;
        }
        if (graphOptions.getGraphName() != null) {
            graphStatement.setGraphName(graphOptions.getGraphName());
        }
        if (graphOptions.getGraphReadConsistencyLevel() != null) {
            graphStatement.setGraphReadConsistencyLevel(graphOptions.getGraphReadConsistencyLevel());
        }
        if (graphOptions.getGraphWriteConsistencyLevel() != null) {
            graphStatement.setGraphWriteConsistencyLevel(graphOptions.getGraphWriteConsistencyLevel());
        }
        if (graphOptions.getGraphSource() != null) {
            graphStatement.setGraphSource(graphOptions.getGraphSource());
        }
        if (graphOptions.getReadTimeoutMillis() != 0) {
            graphStatement.setReadTimeoutMillis(graphOptions.getReadTimeoutMillis());
        }
    }

    /**
     * Builder class for creating a {@link DseRemoteConnection}
     */
    public static class Builder {
        private final DseSession dseSession;
        private GraphOptions graphOptions;

        private Builder(DseSession dseSession) {
            this.dseSession = dseSession;
        }

        /**
         * Additional {@link GraphOptions} to be used for this particular {@link DseRemoteConnection}.
         * <p/>
         * <p/>
         * {@link GraphOptions} defined here will override the options defined on the {@link DseSession}.
         *
         * @param graphOptions the graph options to apply for this {@link RemoteConnection} implementation.
         * @return this builder (for method chaining).
         */
        public Builder withGraphOptions(GraphOptions graphOptions) {
            this.graphOptions = graphOptions;
            return this;
        }

        /**
         * Build the {@link DseRemoteConnection}.
         *
         * @return the remote connection object.
         */
        public DseRemoteConnection build() {
            return new DseRemoteConnection(this);
        }
    }

}
