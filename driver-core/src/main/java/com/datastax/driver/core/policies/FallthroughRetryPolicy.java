/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.policies;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.WriteType;
import com.datastax.driver.core.exceptions.DriverException;

/**
 * A retry policy that never retries (nor ignores).
 * <p/>
 * All of the methods of this retry policy unconditionally return {@link RetryDecision#rethrow()}.
 * If this policy is used, retry logic will have to be implemented in business code.
 */
public class FallthroughRetryPolicy implements RetryPolicy {

    public static final FallthroughRetryPolicy INSTANCE = new FallthroughRetryPolicy();

    private FallthroughRetryPolicy() {
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation always returns {@code RetryDecision.rethrow()}.
     */
    @Override
    public RetryDecision onReadTimeout(Statement statement, ConsistencyLevel cl, int requiredResponses, int receivedResponses, boolean dataRetrieved, int nbRetry) {
        return RetryDecision.rethrow();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation always returns {@code RetryDecision.rethrow()}.
     */
    @Override
    public RetryDecision onWriteTimeout(Statement statement, ConsistencyLevel cl, WriteType writeType, int requiredAcks, int receivedAcks, int nbRetry) {
        return RetryDecision.rethrow();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation always returns {@code RetryDecision.rethrow()}.
     */
    @Override
    public RetryDecision onUnavailable(Statement statement, ConsistencyLevel cl, int requiredReplica, int aliveReplica, int nbRetry) {
        return RetryDecision.rethrow();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation always returns {@code RetryDecision.rethrow()}.
     */
    @Override
    public RetryDecision onRequestError(Statement statement, ConsistencyLevel cl, DriverException e, int nbRetry) {
        return RetryDecision.rethrow();
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
