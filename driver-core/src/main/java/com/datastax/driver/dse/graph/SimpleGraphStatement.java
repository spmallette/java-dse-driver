/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.SimpleStatement;
import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple graph statement implementation.
 */
public class SimpleGraphStatement extends RegularGraphStatement {

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
        maybeRebuildCache();
        return statement;
    }

    @Override
    public GraphStatement executingAs(String userOrRole) {
        this.authorizationId = userOrRole;
        needsRebuild = true;
        return this;
    }

    private void maybeRebuildCache() {
        if (needsRebuild) {
            if (valuesMap.isEmpty()) {
                statement = new SimpleStatement(query);
            } else {
                String values = GraphJsonUtils.writeValueAsString(valuesMap);
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
