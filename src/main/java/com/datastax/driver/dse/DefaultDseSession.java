/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.AddressTranslator;
import com.datastax.driver.dse.graph.GraphOptions;
import com.datastax.driver.dse.graph.GraphResultSet;
import com.datastax.driver.dse.graph.GraphStatement;
import com.datastax.driver.dse.graph.SimpleGraphStatement;
import com.google.common.base.Function;
import com.google.common.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Default implementation of {@link DseSession} interface.
 */
class DefaultDseSession implements DseSession {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDseSession.class);

    private static final Function<ResultSet, GraphResultSet> TO_GRAPH_RESULT_SET = new Function<ResultSet, GraphResultSet>() {
        @Override
        public GraphResultSet apply(ResultSet input) {
            return new GraphResultSet(input);
        }
    };

    private static final String ANALYTICS_GRAPH_SOURCE = "a";
    private static final Statement LOOKUP_ANALYTICS_GRAPH_SERVER = new SimpleStatement("CALL DseClientTool.getAnalyticsGraphServer()");

    private final Session delegate;
    private final Cluster cluster;
    private final GraphOptions graphOptions;
    private final AddressTranslator addressTranslator;
    private final int port;

    DefaultDseSession(Session delegate, GraphOptions graphOptions) {
        this.delegate = delegate;
        this.cluster = delegate.getCluster();
        this.addressTranslator = this.cluster.getConfiguration().getPolicies().getAddressTranslator();
        this.port = this.cluster.getConfiguration().getProtocolOptions().getPort();
        this.graphOptions = graphOptions;
    }

    @Override
    public DseSession init() {
        try {
            return (DseSession) Uninterruptibles.getUninterruptibly(initAsync());
        } catch (ExecutionException e) {
            throw DriverThrowables.propagateCause(e);
        }
    }

    @Override
    public ListenableFuture<Session> initAsync() {
        return Futures.transform(delegate.initAsync(), new Function<Session, Session>() {
            @Override
            public Session apply(Session input) {
                return DefaultDseSession.this;
            }
        });
    }

    @Override
    public GraphResultSet executeGraph(String query) {
        return executeGraph(new SimpleGraphStatement(query));
    }

    @Override
    public GraphResultSet executeGraph(String query, Map<String, Object> values) {
        return executeGraph(new SimpleGraphStatement(query, values));
    }

    @Override
    public GraphResultSet executeGraph(GraphStatement statement) {
        try {
            return Uninterruptibles.getUninterruptibly(executeGraphAsync(statement));
        } catch (ExecutionException e) {
            throw DriverThrowables.propagateCause(e);
        }
    }

    @Override
    public ListenableFuture<GraphResultSet> executeGraphAsync(String query) {
        return executeGraphAsync(new SimpleGraphStatement(query));
    }

    @Override
    public ListenableFuture<GraphResultSet> executeGraphAsync(String query, Map<String, Object> values) {
        return executeGraphAsync(new SimpleGraphStatement(query, values));
    }

    @Override
    public ListenableFuture<GraphResultSet> executeGraphAsync(GraphStatement graphStatement) {
        final Statement statement = graphStatement.unwrap();
        statement.setOutgoingPayload(graphOptions.buildPayloadWithDefaults(graphStatement));

        if (ANALYTICS_GRAPH_SOURCE.equals(graphStatement.getGraphSource())) {
            // Try to send the statement directly to the graph analytics server (we have to look it up first)
            ListenableFuture<ResultSet> serverLocation = Futures.withFallback(
                    delegate.executeAsync(LOOKUP_ANALYTICS_GRAPH_SERVER),
                    new FutureFallback<ResultSet>() {
                        @Override
                        public ListenableFuture<ResultSet> create(Throwable t) throws Exception {
                            logger.debug("Error querying graph analytics server, query will not be routed optimally", t);
                            return null;
                        }
                    });
            return Futures.transform(serverLocation, new AsyncFunction<ResultSet, GraphResultSet>() {
                @Override
                public ListenableFuture<GraphResultSet> apply(ResultSet rs) throws Exception {
                    Host analyticsServer = (rs == null) ? null : extractHostFromAnalyticsServerQuery(rs);
                    Statement targetedStatement = (analyticsServer == null)
                            ? statement
                            : new HostTargetingStatement(statement, analyticsServer);
                    return Futures.transform(delegate.executeAsync(targetedStatement), TO_GRAPH_RESULT_SET);
                }
            });
        } else {
            return Futures.transform(delegate.executeAsync(statement), TO_GRAPH_RESULT_SET);
        }
    }

    private Host extractHostFromAnalyticsServerQuery(ResultSet rs) {
        if (rs.isExhausted()) {
            logger.debug("Empty response querying graph analytics server, query will not be routed optimally");
            return null;
        }

        try {
            Map<String, String> result = rs.one().getMap("result", String.class, String.class);
            if (result != null && result.containsKey("location")) {
                String location = result.get("location");
                String hostName = location.substring(0, location.lastIndexOf(":"));
                InetSocketAddress broadcastRpcAddress = this.addressTranslator.translate(new InetSocketAddress(hostName, port));
                // TODO it would make sense to expose a 'getHostBySocketAddress' in the core to avoid the iteration
                for (Host host : cluster.getMetadata().getAllHosts()) {
                    if (host.getSocketAddress().equals(broadcastRpcAddress)) {
                        logger.debug("Routing analytics query to {}", host);
                        return host;
                    }
                }
                logger.debug("Could not find host matching graph analytics server {}, query will not be routed optimally",
                        broadcastRpcAddress);
                return null;
            }

            logger.debug("Could not extract graph analytics server location from '{}', query will not be routed optimally",
                    result);
            return null;

        } catch (Exception e) {
            logger.debug("Error while processing graph analytics server location, query will not be routed optimally", e);
            return null;
        }
    }

    @Override
    public Cluster getCluster() {
        return delegate.getCluster();
    }

    @Override
    public String getLoggedKeyspace() {
        return delegate.getLoggedKeyspace();
    }

    @Override
    public CloseFuture closeAsync() {
        return delegate.closeAsync();
    }

    @Override
    public boolean isClosed() {
        return delegate.isClosed();
    }

    @Override
    public State getState() {
        return delegate.getState();
    }

    @Override
    public ResultSetFuture executeAsync(Statement statement) {
        return delegate.executeAsync(statement);
    }

    @Override
    public ResultSet execute(String query) {
        return delegate.execute(query);
    }

    @Override
    public ResultSet execute(String query, Object... values) {
        return delegate.execute(query, values);
    }

    @Override
    public ResultSet execute(String query, Map<String, Object> values) {
        return delegate.execute(query, values);
    }

    @Override
    public ResultSet execute(Statement statement) {
        return delegate.execute(statement);
    }

    @Override
    public ResultSetFuture executeAsync(String query) {
        return delegate.executeAsync(query);
    }

    @Override
    public ResultSetFuture executeAsync(String query, Object... values) {
        return delegate.executeAsync(query, values);
    }

    @Override
    public ResultSetFuture executeAsync(String query, Map<String, Object> values) {
        return delegate.executeAsync(query, values);
    }

    @Override
    public PreparedStatement prepare(String query) {
        return delegate.prepare(query);
    }

    @Override
    public PreparedStatement prepare(RegularStatement statement) {
        return delegate.prepare(statement);
    }

    @Override
    public ListenableFuture<PreparedStatement> prepareAsync(String query) {
        return delegate.prepareAsync(query);
    }

    @Override
    public ListenableFuture<PreparedStatement> prepareAsync(RegularStatement statement) {
        return delegate.prepareAsync(statement);
    }

    @Override
    public void close() {
        delegate.close();
    }
}
