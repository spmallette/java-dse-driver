/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.policies.IdempotenceAwareRetryPolicy;
import com.datastax.driver.core.policies.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Add warnings to the parent policy.
 * <p/>
 * We'll backport those warnings to driver-core in version 3.1.0, so this policy should become obsolete when driver-dse
 * depends on 3.1.0.
 */
public class LoggingIdempotenceAwareRetryPolicy extends IdempotenceAwareRetryPolicy {

    private static final Logger logger = LoggerFactory.getLogger(LoggingIdempotenceAwareRetryPolicy.class);

    private final AtomicBoolean warned = new AtomicBoolean();

    /**
     * Creates a new instance.
     *
     * @param childPolicy the policy to wrap.
     */
    public LoggingIdempotenceAwareRetryPolicy(RetryPolicy childPolicy) {
        super(childPolicy);
    }

    @Override
    public void init(Cluster cluster) {
        super.init(cluster);
        warn("Initializing cluster with idempotence-aware retry policy");
    }

    @Override
    protected boolean isIdempotent(Statement statement) {
        boolean isIdempotent = super.isIdempotent(statement);
        if (warned.compareAndSet(false, true) && !isIdempotent)
            warn("Not retrying statement because it is not idempotent (this message will be logged only once)");
        return isIdempotent;
    }

    private void warn(String message) {
        logger.warn("{}. Note that this version of the driver changes the default retry behavior for non-idempotent " +
                "statements: they won't be automatically retried anymore. The driver marks statements non-idempotent " +
                "by default, so you should explicitly call setIdempotent(true) if your statements are safe to retry. " +
                "See http://goo.gl/4HrSby for more details.", message);
    }
}
