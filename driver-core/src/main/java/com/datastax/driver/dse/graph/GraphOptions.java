/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ProtocolVersion;
import com.google.common.collect.ImmutableMap;

import java.nio.ByteBuffer;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
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
    static final String REQUEST_TIMEOUT_KEY = "request-timeout";
    static final String GRAPH_RESULTS_KEY = "graph-results";

    /**
     * The default value for {@link #getGraphLanguage()} ({@value}).
     */
    public static final String DEFAULT_GRAPH_LANGUAGE = "gremlin-groovy";

    /**
     * The default value for {@link #getGraphSource()} ({@value}).
     */
    public static final String DEFAULT_GRAPH_SOURCE = "g";

    private volatile String graphLanguage = DEFAULT_GRAPH_LANGUAGE;
    private volatile String graphSource = DEFAULT_GRAPH_SOURCE;
    private volatile String graphName;
    private volatile ConsistencyLevel graphReadConsistency;
    private volatile ConsistencyLevel graphWriteConsistency;

    private volatile Map<String, ByteBuffer> defaultPayload;

    private volatile int readTimeoutMillis = 0;

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
     * Return the per-host socket read timeout that is set for all graph queries.
     *
     * @return the timeout.
     */
    public int getReadTimeoutMillis() {
        return this.readTimeoutMillis;
    }

    /**
     * Sets the per-host read timeout in milliseconds for graph queries. The default is 0, which means the driver
     * will wait until the coordinator responds with the result or an error, or times out.
     * <p/>
     * Only call this method if you want to wait less than the server's default timeout (defined in {@code dse.yaml}).
     * Note that the server will abort a query once the client has stopped waiting for it, so there's no risk of leaving
     * long-running queries on the server.
     *
     * @param readTimeoutMillis the timeout to set.
     * @return this {@link GraphOptions} instance (for method chaining).
     */
    public GraphOptions setReadTimeoutMillis(int readTimeoutMillis) {
        checkArgument(readTimeoutMillis >= 0, "readTimeoutMillis can not be negative");
        this.readTimeoutMillis = readTimeoutMillis;
        return this;
    }

    /**
     * This method is deprecated because a {@link ProtocolVersion} is required to construct
     * a valid payload. Use {@link #buildPayloadWithDefaults(GraphStatement, ProtocolVersion)} instead.
     * <p/>
     * This method will call {@code GraphOptions#buildPayloadWithDefault(statement, ProtocolVersion.NEWEST_SUPPORTED)}.
     *
     * @param statement the statement.
     * @return the payload.
     */
    @Deprecated
    public Map<String, ByteBuffer> buildPayloadWithDefaults(GraphStatement statement) {
        return buildPayloadWithDefaults(statement, ProtocolVersion.NEWEST_SUPPORTED);
    }

    /**
     * Builds the custom payload for the given statement, providing defaults from these graph options if necessary.
     * <p/>
     * This method is intended for internal use only.

     * @param statement the statement.
     *        protocolVersion the protocol version to use.
     * @return the payload.
     */
 public Map<String, ByteBuffer> buildPayloadWithDefaults(GraphStatement statement, ProtocolVersion protocolVersion) {
        if (statement.getGraphLanguage() == null
                && statement.getGraphSource() == null
                && statement.getGraphReadConsistencyLevel() == null
                && statement.getGraphWriteConsistencyLevel() == null
                && statement.getGraphName() == null
                && statement.getGraphInternalOptions().size() == 0
                && !statement.isSystemQuery()
                && protocolVersion.toInt() >= ProtocolVersion.DSE_V1.toInt()) {
            return defaultPayload;
        } else {
            ImmutableMap.Builder<String, ByteBuffer> builder = ImmutableMap.builder();

            setOrDefaultText(builder, GRAPH_LANGUAGE_KEY, statement.getGraphLanguage(), protocolVersion);
            setOrDefaultText(builder, GRAPH_SOURCE_KEY, statement.getGraphSource(), protocolVersion);

            // ----- Optional DSEGraph settings -----
            setOrDefaultCl(builder, GRAPH_READ_CONSISTENCY_KEY, statement.getGraphReadConsistencyLevel(), protocolVersion);
            setOrDefaultCl(builder, GRAPH_WRITE_CONSISTENCY_KEY, statement.getGraphWriteConsistencyLevel(), protocolVersion);
            if (!statement.isSystemQuery()) {
                setOrDefaultText(builder, GRAPH_NAME_KEY, statement.getGraphName(), protocolVersion);
            }
            if (statement.getReadTimeoutMillis() > 0) {
                // If > 0 it means it's not the default and has to be in the payload.
                setOrDefaultBigInt(builder, REQUEST_TIMEOUT_KEY, (long) statement.getReadTimeoutMillis(), protocolVersion);
            }
            if (protocolVersion.toInt() >= ProtocolVersion.DSE_V1.toInt()) {
                setOrDefaultText(builder, GRAPH_RESULTS_KEY, "graphson-2.0", protocolVersion);
            }

            for (Map.Entry<String, String> optionEntry : statement.getGraphInternalOptions().entrySet()) {
                setOrDefaultText(builder, optionEntry.getKey(), optionEntry.getValue(), protocolVersion);
            }

            return builder.build();
        }
    }

    void setOrDefaultText(ImmutableMap.Builder<String, ByteBuffer> builder, String key, String value, ProtocolVersion protocolVersion) {
        ByteBuffer bytes = (value == null)
                ? defaultPayload.get(key)
                : PayloadHelper.asBytes(value, protocolVersion);

        if (bytes != null)
            builder.put(key, bytes);
    }

    private void setOrDefaultCl(ImmutableMap.Builder<String, ByteBuffer> builder, String key, ConsistencyLevel value, ProtocolVersion protocolVersion) {
        ByteBuffer bytes = (value == null)
                ? defaultPayload.get(key)
                : PayloadHelper.asBytes(value.name(), protocolVersion);

        if (bytes != null)
            builder.put(key, bytes);
    }

    private void setOrDefaultBigInt(ImmutableMap.Builder<String, ByteBuffer> builder, String key, Long value, ProtocolVersion protocolVersion) {
        ByteBuffer bytes = (value == null)
                ? defaultPayload.get(key)
                : PayloadHelper.asBytes(value, protocolVersion);

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
        builder.put(GRAPH_RESULTS_KEY, PayloadHelper.asBytes("graphson-2.0"));

        this.defaultPayload = builder.build();
    }

}
