/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.ConsistencyLevel;
import com.google.common.collect.ImmutableMap;

import java.nio.ByteBuffer;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The default graph options to use for a DSE cluster.
 * <p/>
 * These options will be used for all graph statements sent to the cluster, unless they have been explicitly overridden
 * at the statement level (by using methods such as {@link GraphStatement#setGraphName(String)}).
 * <p/>
 * Graph options are specified at cluster initialization with
 * {@link com.datastax.driver.dse.DseCluster.Builder#withGraphOptions(GraphOptions)}, and can be retrieved at runtime
 * with {@code dseCluster.getConfiguration().getGraphOptions()}.
 */
public class GraphOptions {

    // Static keys for the custom payload maps
    static final String GRAPH_SOURCE_KEY = "graph-source";
    static final String GRAPH_NAME_KEY = "graph-name";
    static final String GRAPH_LANGUAGE_KEY = "graph-language";
    static final String GRAPH_READ_CONSISTENCY_KEY = "graph-read-consistency";
    static final String GRAPH_WRITE_CONSISTENCY_KEY = "graph-write-consistency";

    /**
     * The default value for {@link #getGraphLanguage()} ({@value}).
     */
    public static final String DEFAULT_GRAPH_LANGUAGE = "gremlin-groovy";

    /**
     * The default value for {@link #getGraphSource()} ({@value}).
     */
    public static final String DEFAULT_GRAPH_SOURCE = "default";

    private volatile String graphLanguage = DEFAULT_GRAPH_LANGUAGE;
    private volatile String graphSource = DEFAULT_GRAPH_SOURCE;
    private volatile String graphName;
    private volatile ConsistencyLevel graphReadConsistency;
    private volatile ConsistencyLevel graphWriteConsistency;

    private volatile Map<String, ByteBuffer> defaultPayload;

    public GraphOptions() {
        rebuildDefaultPayload();
    }

    /**
     * Returns the graph language to use in graph queries.
     *
     * @return the graph language to use in graph queries.
     * @see #setGraphLanguage(String)
     */
    public String getGraphLanguage() {
        return graphLanguage;
    }

    /**
     * Sets the graph language to use in graph queries.
     * <p/>
     * This property is required. If you don't call this method, it defaults to {@value #DEFAULT_GRAPH_LANGUAGE}.
     *
     * @param graphLanguage the graph language to use in graph queries.
     * @return this {@code GraphOptions} instance (for method chaining).
     */
    public GraphOptions setGraphLanguage(String graphLanguage) {
        checkNotNull(graphLanguage, "graphLanguage cannot be null");
        this.graphLanguage = graphLanguage;
        rebuildDefaultPayload();
        return this;
    }

    /**
     * Returns the graph traversal source name to use in graph queries.
     *
     * @return The graph traversal source name to use in graph queries.
     * @see #setGraphSource(String)
     */
    public String getGraphSource() {
        return graphSource;
    }

    /**
     * Sets the graph traversal source name to use in graph queries.
     * <p/>
     * This property is required. If you don't call this method, it defaults to {@value #DEFAULT_GRAPH_SOURCE}.
     *
     * @param graphSource the graph traversal source name to use in graph queries.
     * @return this {@code GraphOptions} instance (for method chaining).
     */
    public GraphOptions setGraphSource(String graphSource) {
        checkNotNull(graphSource, "graphSource cannot be null");
        this.graphSource = graphSource;
        rebuildDefaultPayload();
        return this;
    }

    /**
     * Returns the graph name to use in graph queries.
     *
     * @return The graph name to use in graph queries.
     * @see #setGraphName(String)
     */
    public String getGraphName() {
        return graphName;
    }

    /**
     * Sets the graph name to use in graph queries.
     * <p/>
     * This property is optional. If you don't call this method, it is left unset.
     *
     * @param graphName The Graph name to use in graph queries.
     * @return this {@code GraphOptions} instance (for method chaining).
     */
    public GraphOptions setGraphName(String graphName) {
        this.graphName = graphName;
        rebuildDefaultPayload();
        return this;
    }

    /**
     * Returns the read consistency level to use in graph queries.
     *
     * @return the read consistency level configured with graph queries.
     */
    public ConsistencyLevel getGraphReadConsistencyLevel() {
        return this.graphReadConsistency;
    }

    /**
     * Sets the read consistency level to use for graph queries.
     * <p/>
     * This setting will override the consistency level set with {@link GraphStatement#setConsistencyLevel(ConsistencyLevel)}
     * only for the READ part of the graph query.
     * <p/>
     * Please see {@link GraphStatement#setConsistencyLevel(ConsistencyLevel)} for more information.
     *
     * @param cl the consistency level to set.
     * @return this {@link GraphOptions} instance (for method chaining).
     */
    public GraphOptions setGraphReadConsistencyLevel(ConsistencyLevel cl) {
        this.graphReadConsistency = cl;
        rebuildDefaultPayload();
        return this;
    }

    /**
     * Returns the write consistency level to use in graph queries.
     *
     * @return the write consistency level configured with graph queries.
     */
    public ConsistencyLevel getGraphWriteConsistencyLevel() {
        return this.graphWriteConsistency;
    }

    /**
     * Sets the write consistency level to use for graph queries.
     * <p/>
     * This setting will override the consistency level set with {@link GraphStatement#setConsistencyLevel(ConsistencyLevel)}
     * only for the write part of the graph query.
     * <p/>
     * Please see {@link GraphStatement#setConsistencyLevel(ConsistencyLevel)} for more information.
     *
     * @param cl the consistency level to set.
     * @return this {@link GraphStatement} instance (for method chaining).
     */
    public GraphOptions setGraphWriteConsistencyLevel(ConsistencyLevel cl) {
        this.graphWriteConsistency = cl;
        rebuildDefaultPayload();
        return this;
    }

    /**
     * >>>>>>> JAVA-1104: add Native CL and Timestamp and graph CL to GraphOptions and Statements.
     * Builds the custom payload for the given statement, providing defaults from these graph options if necessary.
     * <p/>
     * This method is intended for internal use only.
     *
     * @param statement the statement.
     * @return the payload.
     */
    public Map<String, ByteBuffer> buildPayloadWithDefaults(GraphStatement statement) {
        if (statement.getGraphLanguage() == null
                && statement.getGraphSource() == null
                && statement.getGraphReadConsistencyLevel() == null
                && statement.getGraphWriteConsistencyLevel() == null
                && statement.getGraphName() == null
                && !statement.isSystemQuery()) {
            return defaultPayload;
        } else {
            ImmutableMap.Builder<String, ByteBuffer> builder = ImmutableMap.builder();

            setOrDefault(builder, GRAPH_LANGUAGE_KEY, statement.getGraphLanguage());
            setOrDefault(builder, GRAPH_SOURCE_KEY, statement.getGraphSource());
            if (!statement.isSystemQuery())
                setOrDefault(builder, GRAPH_NAME_KEY, statement.getGraphName());
            setOrDefault(builder, GRAPH_READ_CONSISTENCY_KEY, statement.getGraphReadConsistencyLevel());
            setOrDefault(builder, GRAPH_WRITE_CONSISTENCY_KEY, statement.getGraphWriteConsistencyLevel());

            return builder.build();
        }
    }

    private void setOrDefault(ImmutableMap.Builder<String, ByteBuffer> builder, String key, Object value) {
        ByteBuffer bytes = (value == null) ? defaultPayload.get(key) : PayloadHelper.asBytes(value.toString());

        if (bytes != null)
            builder.put(key, bytes);
    }

    void rebuildDefaultPayload() {
        ImmutableMap.Builder<String, ByteBuffer> builder = ImmutableMap.builder();

        builder.put(GRAPH_LANGUAGE_KEY, PayloadHelper.asBytes(this.graphLanguage));
        builder.put(GRAPH_SOURCE_KEY, PayloadHelper.asBytes(this.graphSource));
        if (this.graphName != null) {
            builder.put(GRAPH_NAME_KEY, PayloadHelper.asBytes(this.graphName));
        }
        if (this.graphReadConsistency != null) {
            builder.put(GRAPH_READ_CONSISTENCY_KEY, PayloadHelper.asBytes(this.graphReadConsistency.name()));
        }
        if (this.graphWriteConsistency != null) {
            builder.put(GRAPH_WRITE_CONSISTENCY_KEY, PayloadHelper.asBytes(this.graphWriteConsistency.name()));
        }

        this.defaultPayload = builder.build();
    }

}
