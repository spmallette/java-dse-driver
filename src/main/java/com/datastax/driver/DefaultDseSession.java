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
package com.datastax.driver;

import com.datastax.driver.core.*;
import com.datastax.driver.graph.GraphOptions;
import com.datastax.driver.graph.GraphResultSet;
import com.datastax.driver.graph.GraphStatement;
import com.datastax.driver.graph.SimpleGraphStatement;
import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Uninterruptibles;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Default implementation of {@link DseSession} interface.
 */
class DefaultDseSession implements DseSession {

    private final Session delegate;
    private final GraphOptions graphOptions;

    DefaultDseSession(Session delegate, GraphOptions graphOptions) {
        this.delegate = delegate;
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
        Statement statement = graphStatement.unwrap();
        statement.setOutgoingPayload(graphOptions.buildPayloadWithDefaults(graphStatement));
        ResultSetFuture resultSetFuture = delegate.executeAsync(statement);
        return Futures.transform(resultSetFuture, new Function<ResultSet, GraphResultSet>() {
            @Override
            public GraphResultSet apply(ResultSet input) {
                return new GraphResultSet(input);
            }
        });
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
