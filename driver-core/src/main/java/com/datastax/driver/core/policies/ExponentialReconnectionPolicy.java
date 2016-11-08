/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.policies;

import com.datastax.driver.core.Cluster;

/**
 * A reconnection policy that waits exponentially longer between each
 * reconnection attempt (but keeps a constant delay once a maximum delay is
 * reached).
 */
public class ExponentialReconnectionPolicy implements ReconnectionPolicy {

    private final long baseDelayMs;
    private final long maxDelayMs;
    private final long maxAttempts;

    /**
     * Creates a reconnection policy waiting exponentially longer for each new attempt.
     *
     * @param baseDelayMs the base delay in milliseconds to use for
     *                    the schedules created by this policy.
     * @param maxDelayMs  the maximum delay to wait between two attempts.
     */
    public ExponentialReconnectionPolicy(long baseDelayMs, long maxDelayMs) {
        if (baseDelayMs < 0 || maxDelayMs < 0)
            throw new IllegalArgumentException("Invalid negative delay");
        if (baseDelayMs == 0)
            throw new IllegalArgumentException("baseDelayMs must be strictly positive");
        if (maxDelayMs < baseDelayMs)
            throw new IllegalArgumentException(String.format("maxDelayMs (got %d) cannot be smaller than baseDelayMs (got %d)", maxDelayMs, baseDelayMs));

        this.baseDelayMs = baseDelayMs;
        this.maxDelayMs = maxDelayMs;

        // Maximum number of attempts after which we overflow (which is kind of theoretical anyway, you'll
        // die of old age before reaching that but hey ...)
        int ceil = (baseDelayMs & (baseDelayMs - 1)) == 0 ? 0 : 1;
        this.maxAttempts = 64 - Long.numberOfLeadingZeros(Long.MAX_VALUE / baseDelayMs) - ceil;
    }

    /**
     * The base delay in milliseconds for this policy (e.g. the delay before
     * the first reconnection attempt).
     *
     * @return the base delay in milliseconds for this policy.
     */
    public long getBaseDelayMs() {
        return baseDelayMs;
    }

    /**
     * The maximum delay in milliseconds between reconnection attempts for this policy.
     *
     * @return the maximum delay in milliseconds between reconnection attempts for this policy.
     */
    public long getMaxDelayMs() {
        return maxDelayMs;
    }

    /**
     * A new schedule that used an exponentially growing delay between reconnection attempts.
     * <p/>
     * For this schedule, reconnection attempt {@code i} will be tried
     * {@code Math.min(2^(i-1) * getBaseDelayMs(), getMaxDelayMs())} milliseconds after the previous one.
     *
     * @return the newly created schedule.
     */
    @Override
    public ReconnectionSchedule newSchedule() {
        return new ExponentialSchedule();
    }

    private class ExponentialSchedule implements ReconnectionSchedule {

        private int attempts;

        @Override
        public long nextDelayMs() {

            if (attempts > maxAttempts)
                return maxDelayMs;

            return Math.min(baseDelayMs * (1L << attempts++), maxDelayMs);
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
