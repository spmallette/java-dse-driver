/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

/**
 * A {@code PercentileTracker} that maintains a separate histogram for each host.
 * <p/>
 * This gives you per-host latency percentiles, meaning that each host will only be compared to itself.
 */
public class PerHostPercentileTracker extends PercentileTracker {
    private PerHostPercentileTracker(long highestTrackableLatencyMillis,
                                     int numberOfSignificantValueDigits,
                                     int minRecordedValues,
                                     long intervalMs) {
        super(highestTrackableLatencyMillis, numberOfSignificantValueDigits, minRecordedValues, intervalMs);
    }

    @Override
    protected Host computeKey(Host host, Statement statement, Exception exception) {
        return host;
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
    public static class Builder extends PercentileTracker.Builder<Builder, PerHostPercentileTracker> {

        Builder(long highestTrackableLatencyMillis) {
            super(highestTrackableLatencyMillis);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public PerHostPercentileTracker build() {
            return new PerHostPercentileTracker(highestTrackableLatencyMillis, numberOfSignificantValueDigits,
                    minRecordedValues, intervalMs);
        }
    }
}
