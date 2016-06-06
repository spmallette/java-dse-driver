/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.datastax.driver.dse.DseSession;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An executable graph query.
 */
public abstract class GraphStatement {

    private volatile String graphLanguage;

    private volatile String graphSource;

    private volatile String graphName;

    private volatile boolean systemQuery;

    private volatile ConsistencyLevel graphReadConsistencyLevel;

    private volatile ConsistencyLevel graphWriteConsistencyLevel;

    /**
     * Returns the graph language to use with this statement.
     *
     * @return the graph language to use with this statement.
     * @see #setGraphLanguage(String)
     */
    public String getGraphLanguage() {
        return graphLanguage;
    }

    /**
     * Sets the graph language to use with this statement.
     * <p/>
     * This property is not required; if it is not set, the default {@link GraphOptions#getGraphLanguage()} will be
     * used when executing the statement.
     *
     * @param graphLanguage the Graph language to use with this statement.
     * @return this {@link GraphStatement} instance (for method chaining).
     */
    public GraphStatement setGraphLanguage(String graphLanguage) {
        checkNotNull(graphLanguage, "graphLanguage cannot be null");
        this.graphLanguage = graphLanguage;
        return this;
    }

    /**
     * Returns the graph traversal source name to use with this statement.
     *
     * @return the graph traversal source name to use with this statement.
     * @see #setGraphSource(String)
     */
    public String getGraphSource() {
        return graphSource;
    }

    /**
     * Sets the graph traversal source name to use with this statement.
     * <p/>
     * This property is not required; if it is not set, the default {@link GraphOptions#getGraphSource()} will be
     * used when executing the statement.
     *
     * @param graphSource the graph traversal source name to use with this statement.
     * @return this {@link GraphStatement} instance (for method chaining).
     */
    public GraphStatement setGraphSource(String graphSource) {
        checkNotNull(graphSource, "graphSource cannot be null");
        this.graphSource = graphSource;
        return this;
    }

    /**
     * Returns the graph name to use with this statement.
     *
     * @return the graph name to use with this statement.
     * @see #getGraphName()
     */
    public String getGraphName() {
        return graphName;
    }

    /**
     * Sets the graph name to use with this statement.
     * <p/>
     * This property is not required; if it is not set, the default {@link GraphOptions#getGraphName()} (which may
     * itself be unset) will be used when executing the statement.
     * <p/>
     * If a default name is set on the global options, but this statement is a system query that you explicitly want to
     * run without a graph name, use {@link #setSystemQuery()}.
     * <p/>
     * If {@link #setSystemQuery()} was called on this statement previously, setting a graph name forces the statement
     * to be a non-system query again.
     *
     * @param graphName the graph name to use with this statement.
     * @return this {@link GraphStatement} instance (for method chaining).
     */
    public GraphStatement setGraphName(String graphName) {
        checkNotNull(graphName, "graphName cannot be null");
        this.graphName = graphName;
        this.systemQuery = false;
        return this;
    }

    /**
     * Forces this statement to use no graph name, even if a default graph name was defined with
     * {@link GraphOptions#setGraphName(String)}.
     * <p/>
     * If a graph name was previously defined on this statement, it will be reset.
     *
     * @return this {@link GraphStatement} instance (for method chaining).
     */
    public GraphStatement setSystemQuery() {
        this.systemQuery = true;
        this.graphName = null;
        return this;
    }

    /**
     * Returns whether this statement is marked as a system query.
     *
     * @return whether this statement is marked as a system query.
     */
    public boolean isSystemQuery() {
        return this.systemQuery;
    }

    /**
     * Returns the read consistency level to use with this statement.
     *
     * @return the read consistency level configured with this statement.
     */
    public ConsistencyLevel getGraphReadConsistencyLevel() {
        return this.graphReadConsistencyLevel;
    }

    /**
     * Sets the read consistency level to use for this statement.
     * <p/>
     * This setting will override the consistency level set with {@link GraphStatement#setConsistencyLevel(ConsistencyLevel)}
     * only for the READ part of the graph query.
     * <p/>
     * Please see {@link GraphStatement#setConsistencyLevel(ConsistencyLevel)} for more information.
     *
     * @param consistencyLevel the consistency level to set.
     * @return this {@link GraphStatement} instance (for method chaining).
     */
    public GraphStatement setGraphReadConsistencyLevel(ConsistencyLevel consistencyLevel) {
        checkNotNull(consistencyLevel, "graphReadConsistencyLevel cannot be null");
        this.graphReadConsistencyLevel = consistencyLevel;
        return this;
    }

    /**
     * Returns the write consistency level to use with this statement.
     *
     * @return the write consistency level configured with this statement.
     */
    public ConsistencyLevel getGraphWriteConsistencyLevel() {
        return this.graphWriteConsistencyLevel;
    }

    /**
     * Sets the write consistency level to use for this statement.
     * <p/>
     * This setting will override the consistency level set with {@link GraphStatement#setConsistencyLevel(ConsistencyLevel)}
     * only for the write part of the graph query.
     * <p/>
     * Please see {@link GraphStatement#setConsistencyLevel(ConsistencyLevel)} for more information.
     *
     * @param consistencyLevel the consistency level to set.
     * @return this {@link GraphStatement} instance (for method chaining).
     */
    public GraphStatement setGraphWriteConsistencyLevel(ConsistencyLevel consistencyLevel) {
        checkNotNull(consistencyLevel, "graphWriteConsistencyLevel cannot be null");
        this.graphWriteConsistencyLevel = consistencyLevel;
        return this;
    }

    /**
     * Sets the consistency level to use for this statement.
     * <p/>
     * This setting will affect the general consistency when executing the graph query. However
     * executing a graph query on the server side is going to involve the execution of CQL queries to the persistence
     * engine that is Cassandra. Those queries can be both reads and writes and both will have a settable consistency
     * level. Setting only this property will indicate to the server to use this consistency level for both reads and
     * writes in Cassandra. Read or write consistency level can be set separately with respectively
     * {@link GraphStatement#setGraphReadConsistencyLevel(ConsistencyLevel)} and
     * {@link GraphStatement#setGraphWriteConsistencyLevel(ConsistencyLevel)} and will override the consistency set here.
     *
     * @param consistencyLevel the consistency level to set.
     * @return this {@link GraphStatement} instance (for method chaining).
     */
    public abstract GraphStatement setConsistencyLevel(ConsistencyLevel consistencyLevel);

    /**
     * Returns the consistency level to use with this statement.
     * <p/>
     * This will return the consistency level used for reads and writes in Cassandra.
     * <p/>
     * Please see {@link GraphStatement#setConsistencyLevel(ConsistencyLevel)} for more information.
     *
     * @return the consistency level configured in this statement.
     */
    public abstract ConsistencyLevel getConsistencyLevel();

    /**
     * Sets the default timestamp for this query (in microseconds since the epoch).
     * <p/>
     * The actual timestamp that will be used for this query is, in order of
     * preference:
     * <ul>
     * <li>the timestamp specified through this method, if different from
     * {@link Long#MIN_VALUE};</li>
     * <li>the timestamp returned by the {@link com.datastax.driver.core.TimestampGenerator} currently in use,
     * if different from {@link Long#MIN_VALUE}.</li>
     * </ul>
     * If none of these apply, no timestamp will be sent with the query and DseGraph
     * will generate a server-side one.
     *
     * @param defaultTimestamp the default timestamp for this query (must be strictly
     *                         positive).
     * @return this {@link GraphStatement} instance (for method chaining).
     * @see com.datastax.driver.dse.DseCluster.Builder#withTimestampGenerator(com.datastax.driver.core.TimestampGenerator)
     */
    public abstract GraphStatement setDefaultTimestamp(long defaultTimestamp);

    /**
     * The default timestamp for this query.
     *
     * @return the default timestamp (in microseconds since the epoch).
     */
    public abstract long getDefaultTimestamp();

    /**
     * "Unwraps" the current graph statement, that is,
     * returns an executable {@link Statement} object corresponding to this graph statement.
     * <p>
     * This method is intended for internal use only, users wishing to execute graph statements
     * should use {@link DseSession#executeGraph(GraphStatement)}.
     * <p/>
     * This method acts as a bridge between graph statements and
     * regular {@link Statement}s.
     * <p/>
     * Implementations are free to cache the returned {@link Statement} if appropriate.
     *
     * @return an executable {@link Statement}.
     */
    public abstract Statement unwrap();

}
