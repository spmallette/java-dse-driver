/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

/**
 * The type of a Cassandra write query.
 * <p/>
 * This information is returned by Cassandra when a write timeout is raised to
 * indicate what type of write timed out. This information is useful to decide
 * which retry policy to adopt.
 */
public enum WriteType {
    /**
     * A write to a single partition key. Such writes are guaranteed to be atomic and isolated.
     */
    SIMPLE,
    /**
     * A write to a multiple partition key that used the distributed batch log to ensure atomicity
     * (atomicity meaning that if any statement in the batch succeeds, all will eventually succeed).
     */
    BATCH,
    /**
     * A write to a multiple partition key that doesn't use the distributed batch log. Atomicity for such writes is not guaranteed
     */
    UNLOGGED_BATCH,
    /**
     * A counter write (that can be for one or multiple partition key). Such write should not be replayed to avoid over-counting.
     */
    COUNTER,
    /**
     * The initial write to the distributed batch log that Cassandra performs internally before a BATCH write.
     */
    BATCH_LOG,
    /**
     * A conditional write. If a timeout has this {@code WriteType}, the timeout has happened while doing the compare-and-swap for
     * an conditional update. In this case, the update may or may not have been applied.
     */
    CAS;
}
