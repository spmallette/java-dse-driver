/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

/**
 * A {@code PercentileTracker} that aggregates all measurements into a single histogram.
 * <p/>
 * This gives you global latency percentiles for the whole cluster, meaning that latencies of slower hosts will tend to
 * appear in higher percentiles.
 */
public class ClusterWidePercentileTracker extends PercentileTracker {
    private volatile Cluster cluster;

    private ClusterWidePercentileTracker(long highestTrackableLatencyMillis,
                                         int numberOfSignificantValueDigits,
                                         int minRecordedValues,
                                         long intervalMs) {
        super(highestTrackableLatencyMillis, numberOfSignificantValueDigits, minRecordedValues, intervalMs);
    }

    @Override
    public void onRegister(Cluster cluster) {
        this.cluster = cluster;
    }

    @Override
    protected Cluster computeKey(Host host, Statement statement, Exception exception) {
        return cluster;
    }

    /**
     * Returns a builder to create a new instance.
     *
     * @param highestTrackableLatencyMillis the highest expected latency. If a higher value is reported, it will be
     *                                      ignored and a warning will be logged. A good rule of thumb is to set it
     *                                      slightly higher than {@link SocketOptions#getReadTimeoutMillis()}.
     * @return the builder.
     */
    public static Builder builder(long highestTrackableLatencyMillis) {
        return new Builder(highestTrackableLatencyMillis);
    }

    /**
     * Helper class to build {@code PerHostPercentileTracker} instances with a fluent interface.
     */
    public static class Builder extends PercentileTracker.Builder<Builder, ClusterWidePercentileTracker> {

        Builder(long highestTrackableLatencyMillis) {
            super(highestTrackableLatencyMillis);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public ClusterWidePercentileTracker build() {
            return new ClusterWidePercentileTracker(highestTrackableLatencyMillis, numberOfSignificantValueDigits,
                    minRecordedValues, intervalMs);
        }
    }
}
