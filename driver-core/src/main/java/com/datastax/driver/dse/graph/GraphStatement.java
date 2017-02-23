/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.dse.DseSession;
import com.google.common.base.Function;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private volatile Boolean idempotent;

    private volatile Function<Row, GraphNode> transformResultFunction = GraphResultSet.ROW_TO_DEFAULTGRAPHNODE;

    private final Map<String, String> graphInternalOptions = new ConcurrentHashMap<String, String>();

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
     * Sets whether this statement is idempotent.
     *
     * @param idempotent the new value.
     * @return this {@code Statement} object.
     * @see Statement#isIdempotent()
     */
    public GraphStatement setIdempotent(boolean idempotent) {
        this.idempotent = idempotent;
        return this;
    }

    /**
     * Whether this statement is idempotent, i.e. whether it can be applied multiple times
     * without changing the result beyond the initial application.
     *
     * @return whether this statement is idempotent, or {@code null} if it uses the default
     * {@link QueryOptions#getDefaultIdempotence()}.
     * @see Statement#setIdempotent(boolean)
     */
    public Boolean isIdempotent() {
        return idempotent;
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
     * Sets additional graph option. Those options are supposed to be used by advanced customers only. The different
     * options settable here are referenced in the DSE documentation.
     *
     * @param optionKey   the option's name.
     * @param optionValue the option's value. The value is always a String and will be interpreted to the right type
     *                    by the DSE server. To unset a value previously set you can put {@code null} as the option's value.
     * @return this {@link GraphStatement} instance (for method chaining).
     */
    public GraphStatement setGraphInternalOption(String optionKey, String optionValue) {
        checkNotNull(optionKey, "option key cannot be null");

        // Setting null as value means removing it from all payloads/maps.
        if (optionValue == null) {
            getGraphInternalOptions().remove(optionKey);
        } else {
            getGraphInternalOptions().put(optionKey, optionValue);
        }
        return this;
    }

    /**
     * Returns the advanced option's value defined for the key in parameter.
     *
     * @param optionKey the name of the option.
     * @return the value. If no value has previously been set for the specified option name, or if a value has been set
     * then unset with {@code null} (see {@link GraphStatement#setGraphInternalOption}), this method
     * returns {@code null}.
     */
    public String getGraphInternalOption(String optionKey) {
        return getGraphInternalOptions().get(optionKey);
    }

    Map<String, String> getGraphInternalOptions() {
        return graphInternalOptions;
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
     * Return the per-host socket read timeout that was set for this statement.
     *
     * @return the timeout. A negative value means that the timeout has not been set on this statement; the default
     * {@link GraphOptions#getReadTimeoutMillis()} will be used.
     */
    public abstract int getReadTimeoutMillis();

    /**
     * Overrides the default per-host read timeout ({@link GraphOptions#setReadTimeoutMillis(int)}) for this statement.
     * <p/>
     * If you don't call this method, the default {@link GraphOptions#getReadTimeoutMillis()} will be used.
     *
     * @param readTimeoutMillis the timeout to set. {@code 0} will disable the timeout for the query, negative values
     *                          are not allowed.
     * @return this {@link GraphOptions} instance (for method chaining).
     */
    public abstract GraphStatement setReadTimeoutMillis(int readTimeoutMillis);

    /**
     * Allows this statement to be executed as a different user/role
     * than the one currently authenticated (a.k.a. proxy execution).
     * <p/>
     * This feature is only available in DSE 5.1+.
     *
     * @param userOrRole The user or role name to act as when executing this statement.
     */
    public GraphStatement executingAs(String userOrRole) {
        throw new UnsupportedOperationException("This method is concrete only for backward compatibility; " +
                "it must be implemented in subclasses");
    }

    /**
     * "Unwraps" the current graph statement, that is,
     * returns an executable {@link Statement} object corresponding to this graph statement.
     * <p/>
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

    /**
     * Sets the function to transform a {@link com.datastax.driver.core.Row} to a {@link com.datastax.driver.dse.graph.GraphNode}.
     * <p/>
     * See {@link #getTransformResultFunction}.
     *
     * @param transformResultFunction the function to set.
     * @return this {@link GraphStatement} instance (for method chaining).
     */
    public GraphStatement setTransformResultFunction(Function<Row, GraphNode> transformResultFunction) {
        this.transformResultFunction = transformResultFunction;
        return this;
    }

    /**
     * Allows users to input their own implementations of {@link com.datastax.driver.dse.graph.GraphNode}
     * directly into the DSE Driver's workflow. This method must return a function that will
     * take as a input a {@link Row} from the underlying Apache Cassandra driver, and return
     * an object in the type {@link GraphNode}.
     *
     * @return the function that returns a {@link GraphNode} implementation.
     */
    public Function<Row, GraphNode> getTransformResultFunction() {
        return this.transformResultFunction;
    }

}
