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
package com.datastax.driver.dse;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.AuthenticationException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;
import com.datastax.driver.dse.graph.GraphOptions;
import com.datastax.driver.dse.graph.GraphResultSet;
import com.datastax.driver.dse.graph.GraphStatement;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * Holds connections to a DSE cluster, allowing it to be queried.
 * <p/>
 * This extends the CQL driver's {@link Session} with DSE-specific features.
 */
public interface DseSession extends Session {

    /**
     * Forces the initialization of this instance if it hasn't been
     * initialized yet.
     * <p/>
     * Most users won't need to call this method explicitly. If you use {@link DseCluster#connect} to create your
     * session, the returned object will be already initialized. Even if you create a non-initialized session through
     * {@link DseCluster#newSession}, that session will get automatically initialized the first time it is used for
     * querying. This method is thus only useful if you use {@link DseCluster#newSession} and want to explicitly force
     * initialization without querying.
     * <p/>
     * Session initialization consists in connecting to the known Cassandra hosts (at least those that should not be
     * ignored due to the {@code LoadBalancingPolicy} in place).
     * <p/>
     * If the {@link DseCluster} instance this session depends on is not itself initialized, it will be initialized by
     * this method.
     * <p/>
     * If the session is already initialized, this method is a no-op.
     *
     * @return this object.
     * @throws NoHostAvailableException if this initialization triggers the cluster initialization and no host amongst
     *                                  the contact points can be reached.
     * @throws AuthenticationException  if this initialization triggers the cluster initialization and an authentication
     *                                  error occurs while contacting the initial contact points.
     */
    @Override
    DseSession init();

    /**
     * {@inheritDoc}
     * <p/>
     * The {@link Session} object returned by the future's {@link Future#get() get} method can be safely cast
     * to {@link DseSession}.
     *
     * @return a future that will complete when the session is fully initialized.
     * @see #init()
     */
    @Override
    ListenableFuture<Session> initAsync();

    /**
     * {@inheritDoc}
     * <p/>
     * The keyspace name returned by this method is only relevant for CQL queries; for graph queries, the graph to
     * target is determined by the graph name specified via {@link GraphStatement#setGraphName(String)}} or
     * {@link GraphOptions#setGraphName(String)}.
     */
    @Override
    String getLoggedKeyspace();

    /**
     * Executes the provided graph query.
     * <p/>
     * This is a convenience method for {@code execute(new SimpleGraphStatement(query))}.
     *
     * @param query the graph query to execute.
     * @return the result of the query. That result will never be null but can be empty (and will be for any query that
     * returns no results).
     * @throws NoHostAvailableException if no host in the cluster can be contacted successfully to execute this query.
     */
    GraphResultSet executeGraph(String query);

    /**
     * Executes the provided graph query with the provided named parameters.
     * <p/>
     * This is a convenience method for {@code execute(new SimpleGraphStatement(query, values))}.
     *
     * @param query  the graph query to execute.
     * @param values the named parameters to send associated to the query. You can use Guava's
     *               {@link com.google.common.collect.ImmutableMap ImmutableMap} to build the map with a one-liner:
     *               {@code ImmutableMap.<String, Object>of("key1", value1, "key2", value2)}.
     * @return the result of the query. That result will never be null but can be empty (and will be for any query that
     * returns no results).
     * @throws NoHostAvailableException if no host in the cluster can be contacted successfully to execute this query.
     */
    GraphResultSet executeGraph(String query, Map<String, Object> values);

    /**
     * Executes the provided graph query.
     * <p/>
     * This method blocks until at least some result has been received from the
     * database. However, for queries that return a result, it does not guarantee that the
     * result has been received in full. But it does guarantee that some
     * response has been received from the database, and in particular
     * guarantees that if the request is invalid, an exception will be thrown
     * by this method.
     *
     * @param statement the statement to execute.
     * @return the result of the query. That result will never be null but can be empty (and will be for any query that
     * returns no results).
     * @throws NoHostAvailableException if no host in the cluster can be
     *                                  contacted successfully to execute this query.
     * @throws QueryExecutionException  if the query triggered an execution
     *                                  exception, i.e. an exception thrown by Cassandra when it cannot execute
     *                                  the query with the requested consistency level successfully.
     * @throws QueryValidationException if the query if invalid (syntax error,
     *                                  unauthorized or any other validation problem).
     */
    GraphResultSet executeGraph(GraphStatement statement);

    /**
     * Executes the provided graph query asynchronously.
     * <p/>
     * This method does not block. It returns as soon as the query has been
     * passed to the underlying network stack. In particular, returning from
     * this method does not guarantee that the query is valid or has even been
     * submitted to a live node. Any exception pertaining to the failure of the
     * query will be thrown when accessing the {@link ListenableFuture}.
     * <p/>
     * Note that for queries that don't return a result, you will need to
     * access the Future's {@link java.util.concurrent.Future#get() get}
     * method to make sure the query was successful.
     *
     * @param query the graph query to execute.
     * @return a future on the result of the query.
     */
    ListenableFuture<GraphResultSet> executeGraphAsync(String query);

    /**
     * Executes the provided graph query asynchronously with the specified parameters.
     * <p/>
     * This method does not block. It returns as soon as the query has been
     * passed to the underlying network stack. In particular, returning from
     * this method does not guarantee that the query is valid or has even been
     * submitted to a live node. Any exception pertaining to the failure of the
     * query will be thrown when accessing the {@link ListenableFuture}.
     * <p/>
     * Note that for queries that don't return a result, you will need to
     * access the Future's {@link java.util.concurrent.Future#get() get}
     * method to make sure the query was successful.
     *
     * @param query  the graph query to execute.
     * @param values the named parameters to send associated to the query. You can use Guava's
     *               {@link com.google.common.collect.ImmutableMap ImmutableMap} to build the map with a one-liner:
     *               {@code ImmutableMap.<String, Object>of("key1", value1, "key2", value2)}.
     * @return a future on the result of the query.
     */
    ListenableFuture<GraphResultSet> executeGraphAsync(String query, Map<String, Object> values);

    /**
     * Executes the provided Graph query asynchronously.
     * <p/>
     * This method does not block. It returns as soon as the query has been
     * passed to the underlying network stack. In particular, returning from
     * this method does not guarantee that the query is valid or has even been
     * submitted to a live node. Any exception pertaining to the failure of the
     * query will be thrown when accessing the {@link ListenableFuture}.
     * <p/>
     * Note that for queries that don't return a result, you will need to
     * access the Future's {@link java.util.concurrent.Future#get() get}
     * method to make sure the query was successful.
     *
     * @param statement the statement to execute.
     * @return a future on the result of the query.
     */
    ListenableFuture<GraphResultSet> executeGraphAsync(GraphStatement statement);

}
