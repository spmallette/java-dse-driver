/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.policies;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Statement;

/**
 * The policy that decides if the driver will send speculative queries to the next hosts when the current host takes too
 * long to respond.
 * <p/>
 * Note that only idempotent statements will be speculatively retried, see
 * {@link com.datastax.driver.core.Statement#isIdempotent()} for more information.
 */
public interface SpeculativeExecutionPolicy {
    /**
     * Gets invoked at cluster startup.
     *
     * @param cluster the cluster that this policy is associated with.
     */
    void init(Cluster cluster);

    /**
     * Returns the plan to use for a new query.
     *
     * @param loggedKeyspace the currently logged keyspace (the one set through either
     *                       {@link Cluster#connect(String)} or by manually doing a {@code USE} query) for
     *                       the session on which this plan need to be built. This can be {@code null} if
     *                       the corresponding session has no keyspace logged in.
     * @param statement      the query for which to build a plan.
     * @return the plan.
     */
    SpeculativeExecutionPlan newPlan(String loggedKeyspace, Statement statement);

    /**
     * Gets invoked at cluster shutdown.
     * <p/>
     * This gives the policy the opportunity to perform some cleanup, for instance stop threads that it might have started.
     */
    void close();

    /**
     * A plan that governs speculative executions for a given query.
     * <p/>
     * Each time a host is queried, {@link #nextExecution(Host)} is invoked to determine if and when a speculative query to
     * the next host will be sent.
     */
    interface SpeculativeExecutionPlan {
        /**
         * Returns the time before the next speculative query.
         *
         * @param lastQueried the host that was just queried.
         * @return the time (in milliseconds) before a speculative query is sent to the next host. If zero or negative,
         * no speculative query will be sent.
         */
        long nextExecution(Host lastQueried);
    }
}
