/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.policies;

import com.datastax.driver.core.Cluster;

/**
 * A reconnection policy that waits a constant time between each reconnection attempt.
 */
public class ConstantReconnectionPolicy implements ReconnectionPolicy {

    private final long delayMs;

    /**
     * Creates a reconnection policy that creates with the provided constant wait
     * time between reconnection attempts.
     *
     * @param constantDelayMs the constant delay in milliseconds to use.
     */
    public ConstantReconnectionPolicy(long constantDelayMs) {
        if (constantDelayMs < 0)
            throw new IllegalArgumentException(String.format("Invalid negative delay (got %d)", constantDelayMs));

        this.delayMs = constantDelayMs;
    }

    /**
     * The constant delay used by this reconnection policy.
     *
     * @return the constant delay used by this reconnection policy.
     */
    public long getConstantDelayMs() {
        return delayMs;
    }

    /**
     * A new schedule that uses a constant {@code getConstantDelayMs()} delay
     * between reconnection attempt.
     *
     * @return the newly created schedule.
     */
    @Override
    public ReconnectionSchedule newSchedule() {
        return new ConstantSchedule();
    }

    private class ConstantSchedule implements ReconnectionSchedule {

        @Override
        public long nextDelayMs() {
            return delayMs;
        }
    }

    @Override
    public void init(Cluster cluster) {
        // nothing to do
    }

    @Override
    public void close() {
        // nothing to do
    }
}
