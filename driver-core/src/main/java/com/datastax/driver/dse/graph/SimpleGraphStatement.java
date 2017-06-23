/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.SimpleStatement;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple graph statement implementation.
 */
public class SimpleGraphStatement extends RegularGraphStatement {

    private static final Logger logger = LoggerFactory.getLogger(SimpleGraphStatement.class);

    private static final AtomicBoolean WARNED_GRAPHSON1 = new AtomicBoolean(false);

    private final String query;

    private final Map<String, Object> valuesMap;

    private boolean needsRebuild = true;

    private SimpleStatement statement;

    private ConsistencyLevel nativeConsistencyLevel;

    private long defaultTimestamp = Long.MIN_VALUE;

    private volatile int readTimeoutMillis = Integer.MIN_VALUE;

    private String authorizationId;

    public SimpleGraphStatement(String query) {
        this(query, new HashMap<String, Object>());
    }

    public SimpleGraphStatement(String query, Map<String, Object> valuesMap) {
        checkNotNull(valuesMap, "Parameter valuesMap cannot be null");
        this.query = query;
        this.valuesMap = valuesMap;
    }

    @Override
    public String getQueryString() {
        return query;
    }

    /**
     * Sets a parameter on this statement.
     * <p/>
     * Note that, contrary to CQL parameters, which are identified either by
     * a bind marker "?" or by the sign ":" in front of a parameter name,
     * parameters in Gremlin queries are simply referenced by their names.
     * Please refer to Gremlin's documentation for more information.
     * <p/>
     * Parameter values can be of any type supported by the subprotocol in use:
     * Boolean, Integer, Long, Float, Double, String, Map, List, {@link GraphNode},
     * {@link Element}, any geospatial type.
     *
     * @param name  the parameter name, as referenced in the graph query.
     * @param value the parameter value.
     */
    public SimpleGraphStatement set(String name, Object value) {
        this.valuesMap.put(name, value);
        needsRebuild = true;
        return this;
    }

    @Override
    public GraphStatement setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        needsRebuild = true;
        this.nativeConsistencyLevel = consistencyLevel;
        return this;
    }

    @Override
    public ConsistencyLevel getConsistencyLevel() {
        return nativeConsistencyLevel;
    }

    @Override
    public GraphStatement setDefaultTimestamp(long defaultTimestamp) {
        needsRebuild = true;
        this.defaultTimestamp = defaultTimestamp;
        return this;
    }

    public int getReadTimeoutMillis() {
        return this.readTimeoutMillis;
    }

    @Override
    public GraphStatement setReadTimeoutMillis(int readTimeoutMillis) {
        Preconditions.checkArgument(readTimeoutMillis >= 0, "read timeout must be >= 0");
        this.readTimeoutMillis = readTimeoutMillis;
        needsRebuild = true;
        return this;
    }

    @Override
    public long getDefaultTimestamp() {
        return defaultTimestamp;
    }

    @Override
    public SimpleStatement unwrap() {
        return unwrap(GraphProtocol.GRAPHSON_1_0);
    }

    @Override
    public SimpleStatement unwrap(GraphProtocol graphProtocol) {
        maybeRebuildCache(graphProtocol);
        if (graphProtocol != GraphProtocol.GRAPHSON_1_0) {
            // Deserialize correctly GraphSON2 results
            setTransformResultFunction(GraphJsonUtils.ROW_TO_GRAPHSON2_OBJECTGRAPHNODE);
        } else {
            if (WARNED_GRAPHSON1.compareAndSet(false, true)) {
                logger.warn("GraphSON1 is being used for a graph query, however it is recommended " +
                        "to switch to GraphSON2 when executing a graph query to maintain " +
                        "type information in requests and responses to the DSE Graph server. " +
                        "Enabling GraphSON2 can be done via the DseCluster's GraphOptions, " +
                        "see https://goo.gl/EAUBUv for more information.");
            }
        }
        return statement;
    }

    @Override
    public GraphStatement executingAs(String userOrRole) {
        this.authorizationId = userOrRole;
        needsRebuild = true;
        return this;
    }

    private void maybeRebuildCache(GraphProtocol graphProtocol) {
        if (needsRebuild) {
            if (valuesMap.isEmpty()) {
                statement = new SimpleStatement(query);
            } else {
                String values = graphProtocol == GraphProtocol.GRAPHSON_1_0
                        ? GraphJsonUtils.writeValueAsString(valuesMap)
                        : GraphJsonUtils.writeValueAsStringGraphson20(valuesMap);

                statement = new SimpleStatement(query, values);
            }
            if (getConsistencyLevel() != null)
                statement.setConsistencyLevel(nativeConsistencyLevel);
            if (getDefaultTimestamp() != Long.MIN_VALUE)
                statement.setDefaultTimestamp(defaultTimestamp);
            if (getReadTimeoutMillis() != Integer.MIN_VALUE)
                statement.setReadTimeoutMillis(readTimeoutMillis);
            if (this.authorizationId != null)
                statement.executingAs(authorizationId);

            needsRebuild = false;
        }
    }
}
