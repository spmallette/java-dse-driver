/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.SimpleStatement;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple graph statement implementation.
 */
public class SimpleGraphStatement extends RegularGraphStatement {

    private final String query;

    private final Map<String, Object> valuesMap;

    private boolean needsRebuild = true;

    private SimpleStatement statement;

    private ConsistencyLevel nativeConsistencyLevel;

    private long defaultTimestamp = -1;

    public SimpleGraphStatement(String query) {
        this(query, new HashMap<String, Object>());
    }

    public SimpleGraphStatement(String query, Map<String, Object> valuesMap) {
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
     * Parameter values can be of any type supported in JSON:
     * Boolean, Integer, Long, Float, Double, and String.
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

    @Override
    public long getDefaultTimestamp() {
        return defaultTimestamp;
    }

    @Override
    public SimpleStatement unwrap() {
        maybeRebuildCache();
        return statement;
    }

    private void maybeRebuildCache() {
        if (needsRebuild) {
            String values = GraphJsonUtils.convert(valuesMap);
            statement = new SimpleStatement(query, values);
            if (getConsistencyLevel() != null)
                statement.setConsistencyLevel(nativeConsistencyLevel);
            if (getDefaultTimestamp() >= 0)
                statement.setDefaultTimestamp(defaultTimestamp);
            needsRebuild = false;
        }
    }
}
