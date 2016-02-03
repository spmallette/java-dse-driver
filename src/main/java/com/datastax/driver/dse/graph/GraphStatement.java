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
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.Statement;
import com.datastax.driver.dse.DseSession;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An executable graph query.
 */
public abstract class GraphStatement {

    private String graphLanguage;

    private String graphSource;

    private String graphName;

    private String graphAlias;

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
        checkArgument(graphLanguage != GraphOptions.NONE, "graphLanguage must be set");
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
        checkArgument(graphLanguage != GraphOptions.NONE, "graphSource must be set");
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
     * If the default is set on the global options, but you explicitly want to force this statement to execute with no
     * graph name, pass {@link GraphOptions#NONE} to this method.
     *
     * @param graphName the Graph name to use with this statement.
     * @return this {@link GraphStatement} instance (for method chaining).
     */
    public GraphStatement setGraphName(String graphName) {
        checkNotNull(graphName, "graphName cannot be null");
        this.graphName = graphName;
        return this;
    }

    /**
     * Returns the graph alias to use with this statement.
     *
     * @return the graph alias to use with this statement.
     * @see #setGraphAlias(String)
     */
    public String getGraphAlias() {
        return graphAlias;
    }

    /**
     * Sets the graph alias to use with this statement.
     * <p/>
     * This property is not required; if it is not set, the default {@link GraphOptions#getGraphAlias()} (which may
     * itself be unset) will be used when executing the statement.
     * <p/>
     * If the default is set on the global options, but you explicitly want to force this statement to execute with no
     * graph alias, pass {@link GraphOptions#NONE} to this method.
     *
     * @param graphAlias the graph alias to use with this statement.
     * @return this {@link GraphStatement} instance (for method chaining).
     */
    public GraphStatement setGraphAlias(String graphAlias) {
        checkNotNull(graphAlias, "graphAlias cannot be null");
        this.graphAlias = graphAlias;
        return this;
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
