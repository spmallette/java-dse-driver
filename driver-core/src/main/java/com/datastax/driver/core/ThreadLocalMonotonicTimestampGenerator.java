/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;


import java.util.concurrent.TimeUnit;

/**
 * A timestamp generator that guarantees monotonically increasing timestamps on a per-thread basis, and logs warnings
 * when timestamps drift in the future.
 * <p/>
 * Beware that there is a risk of timestamp collision with this generator when accessed
 * by more than one thread at a time; only use it when threads are not in direct competition
 * for timestamp ties (i.e., they are executing independent statements).
 *
 * @see AbstractMonotonicTimestampGenerator
 */
public class ThreadLocalMonotonicTimestampGenerator extends LoggingMonotonicTimestampGenerator {

    // We're deliberately avoiding an anonymous subclass with initialValue(), because this can introduce
    // classloader leaks in managed environments like Tomcat
    private final ThreadLocal<Long> lastRef = new ThreadLocal<Long>();

    /**
     * Creates a new instance with a warning threshold and warning interval of one second.
     *
     * @see #ThreadLocalMonotonicTimestampGenerator(long, TimeUnit, long, TimeUnit)
     */
    public ThreadLocalMonotonicTimestampGenerator() {
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
    public ThreadLocalMonotonicTimestampGenerator(long warningThreshold, TimeUnit warningThresholdUnit,
                                                  long warningInterval, TimeUnit warningIntervalUnit) {
        super(warningThreshold, warningThresholdUnit, warningInterval, warningIntervalUnit);
    }

    @Override
    public long next() {
        Long last = this.lastRef.get();
        if (last == null)
            last = 0L;

        long next = computeNext(last);

        this.lastRef.set(next);
        return next;
    }
}
