/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A timestamp generator that guarantees monotonically increasing timestamps among all client threads, and logs warnings
 * when timestamps drift in the future.
 *
 * @see AbstractMonotonicTimestampGenerator
 */
public class AtomicMonotonicTimestampGenerator extends LoggingMonotonicTimestampGenerator {

    private AtomicLong lastRef = new AtomicLong(0);

    /**
     * Creates a new instance with a warning threshold and warning interval of one second.
     *
     * @see #AtomicMonotonicTimestampGenerator(long, TimeUnit, long, TimeUnit)
     */
    public AtomicMonotonicTimestampGenerator() {
        this(1, TimeUnit.SECONDS, 1, TimeUnit.SECONDS);
    }

    /**
     * Creates a new instance.
     *
     * @param warningThreshold     how far in the future timestamps are allowed to drift before a warning is logged.
     * @param warningThresholdUnit the unit for {@code warningThreshold}.
     * @param warningInterval      how often the warning will be logged if timestamps keep drifting above the threshold.
     * @param warningIntervalUnit  the unit for {@code warningIntervalUnit}.
     */
    public AtomicMonotonicTimestampGenerator(long warningThreshold, TimeUnit warningThresholdUnit,
                                             long warningInterval, TimeUnit warningIntervalUnit) {
        super(warningThreshold, warningThresholdUnit, warningInterval, warningIntervalUnit);
    }

    @Override
    public long next() {
        while (true) {
            long last = lastRef.get();
            long next = computeNext(last);
            if (lastRef.compareAndSet(last, next))
                return next;
        }
    }
}
