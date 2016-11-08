/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.policies;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Statement;

/**
 * A {@link SpeculativeExecutionPolicy} that never schedules speculative executions.
 */
public class NoSpeculativeExecutionPolicy implements SpeculativeExecutionPolicy {

    /**
     * The single instance (this class is stateless).
     */
    public static final NoSpeculativeExecutionPolicy INSTANCE = new NoSpeculativeExecutionPolicy();

    private static final SpeculativeExecutionPlan PLAN = new SpeculativeExecutionPlan() {
        @Override
        public long nextExecution(Host lastQueried) {
            return -1;
        }
    };

    @Override
    public SpeculativeExecutionPlan newPlan(String loggedKeyspace, Statement statement) {
        return PLAN;
    }

    private NoSpeculativeExecutionPolicy() {
        // do nothing
    }

    @Override
    public void init(Cluster cluster) {
        // do nothing
    }

    @Override
    public void close() {
        // do nothing
    }
}
