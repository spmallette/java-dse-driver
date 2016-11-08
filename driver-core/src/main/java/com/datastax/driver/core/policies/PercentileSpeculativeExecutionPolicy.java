/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.policies;

import com.datastax.driver.core.*;

import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A policy that triggers speculative executions when the request to the current host is above a given percentile.
 */
public class PercentileSpeculativeExecutionPolicy implements SpeculativeExecutionPolicy {
    private final PercentileTracker percentileTracker;
    private final double percentile;
    private final int maxSpeculativeExecutions;

    /**
     * Builds a new instance.
     *
     * @param percentileTracker        the component that will record latencies. It will get
     *                                 {@link Cluster#register(LatencyTracker) registered} with the cluster when this
     *                                 policy initializes.
     * @param percentile               the percentile that a request's latency must fall into to be considered slow (ex:
     *                                 {@code 99.0}).
     * @param maxSpeculativeExecutions the maximum number of speculative executions that will be triggered for a given
     *                                 request (this does not include the initial, normal request). Must be strictly
     *                                 positive.
     */
    public PercentileSpeculativeExecutionPolicy(PercentileTracker percentileTracker,
                                                double percentile, int maxSpeculativeExecutions) {
        checkArgument(maxSpeculativeExecutions > 0,
                "number of speculative executions must be strictly positive (was %d)", maxSpeculativeExecutions);
        checkArgument(percentile >= 0.0 && percentile < 100,
                "percentile must be between 0.0 and 100 (was %f)");

        this.percentileTracker = percentileTracker;
        this.percentile = percentile;
        this.maxSpeculativeExecutions = maxSpeculativeExecutions;
    }

    @Override
    public SpeculativeExecutionPlan newPlan(String loggedKeyspace, Statement statement) {
        return new SpeculativeExecutionPlan() {
            private final AtomicInteger remaining = new AtomicInteger(maxSpeculativeExecutions);

            @Override
            public long nextExecution(Host lastQueried) {
                if (remaining.getAndDecrement() > 0)
                    return percentileTracker.getLatencyAtPercentile(lastQueried, null, null, percentile);
                else
                    return -1;
            }
        };
    }

    @Override
    public void init(Cluster cluster) {
        cluster.register(percentileTracker);
    }

    @Override
    public void close() {
        // nothing
    }
}
