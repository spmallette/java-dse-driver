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

    /**
     * A special constant to force an option off from a {@link GraphStatement}.
     * <p/>
     * Not setting an option at the statement level means "use whatever default is defined in {@link GraphOptions}".
     * If instead you want to force the option to "no value" for this specific statement, pass this constant.
     */
    public static final String NONE = new String("NONE");

    // Static keys for the custom payload maps
    private static final String GRAPH_SOURCE_KEY = "graph-source";
    private static final String GRAPH_NAME_KEY = "graph-name";
    private static final String GRAPH_LANGUAGE_KEY = "graph-language";
    private static final String GRAPH_ALIAS_KEY = "graph-alias";

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
    private volatile String graphAlias;

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
        checkArgument(graphLanguage != NONE, "graphLanguage must be set");
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
        checkArgument(graphLanguage != NONE, "graphSource must be set");
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
     * Returns the graph alias to use in graph queries.
     *
     * @return The graph alias to use in graph queries.
     * @see #setGraphAlias(String)
     */
    public String getGraphAlias() {
        return graphAlias;
    }

    /**
     * Sets the graph alias to use in graph queries.
     * <p/>
     * This property is optional. If you don't call this method, it is left unset.
     *
     * @param graphAlias the graph alias to use in graph queries.
     * @return this {@code GraphOptions} instance (for method chaining).
     */
    public GraphOptions setGraphAlias(String graphAlias) {
        this.graphAlias = graphAlias;
        rebuildDefaultPayload();
        return this;
    }

    /**
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
                && statement.getGraphName() == null
                && statement.getGraphAlias() == null) {
            return defaultPayload;
        } else {
            ImmutableMap.Builder<String, ByteBuffer> builder = ImmutableMap.builder();

            setOrDefault(builder, GRAPH_LANGUAGE_KEY, statement.getGraphLanguage());
            setOrDefault(builder, GRAPH_SOURCE_KEY, statement.getGraphSource());
            setOrDefault(builder, GRAPH_NAME_KEY, statement.getGraphName());
            setOrDefault(builder, GRAPH_ALIAS_KEY, statement.getGraphAlias());

            return builder.build();
        }
    }

    private void setOrDefault(ImmutableMap.Builder<String, ByteBuffer> builder, String key, String value) {
        ByteBuffer bytes;
        if (value == null)
            bytes = defaultPayload.get(key);
        else if (value == NONE)
            bytes = null;
        else
            bytes = PayloadHelper.asBytes(value);

        if (bytes != null)
            builder.put(key, bytes);
    }

    private void rebuildDefaultPayload() {
        ImmutableMap.Builder<String, ByteBuffer> builder = ImmutableMap.builder();

        builder.put(GRAPH_LANGUAGE_KEY, PayloadHelper.asBytes(this.graphLanguage));
        builder.put(GRAPH_SOURCE_KEY, PayloadHelper.asBytes(this.graphSource));
        if (this.graphName != null && this.graphName != NONE)
            builder.put(GRAPH_NAME_KEY, PayloadHelper.asBytes(this.graphName));
        if (this.graphAlias != null && this.graphAlias != NONE)
            builder.put(GRAPH_ALIAS_KEY, PayloadHelper.asBytes(this.graphAlias));

        this.defaultPayload = builder.build();
    }

}
