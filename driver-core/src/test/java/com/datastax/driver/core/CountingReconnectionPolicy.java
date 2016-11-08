/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.datastax.driver.core.policies.ReconnectionPolicy;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A reconnection policy that tracks how many times its schedule has been invoked.
 */
public class CountingReconnectionPolicy implements ReconnectionPolicy {
    public final AtomicInteger count = new AtomicInteger();
    private final ReconnectionPolicy childPolicy;

    public CountingReconnectionPolicy(ReconnectionPolicy childPolicy) {
        this.childPolicy = childPolicy;
    }

    @Override
    public ReconnectionSchedule newSchedule() {
        return new CountingSchedule(childPolicy.newSchedule());
    }

    class CountingSchedule implements ReconnectionSchedule {
        private final ReconnectionSchedule childSchedule;

        public CountingSchedule(ReconnectionSchedule childSchedule) {
            this.childSchedule = childSchedule;
        }

        @Override
        public long nextDelayMs() {
            count.incrementAndGet();
            return childSchedule.nextDelayMs();
        }
    }

    @Override
    public void init(Cluster cluster) {
    }

    @Override
    public void close() {
        childPolicy.close();
    }

}
