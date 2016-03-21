/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

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
     * "Unwraps" the current graph statement, i.e.,
     * returns an executable {@link Statement} object corresponding to this graph statement.
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
