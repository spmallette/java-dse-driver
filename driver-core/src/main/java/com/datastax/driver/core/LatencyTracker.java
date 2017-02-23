/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

/**
 * Interface for objects that are interested in tracking the latencies
 * of the driver queries to each Cassandra nodes.
 * <p/>
 * An implementation of this interface can be registered against a Cluster
 * object trough the {@link Cluster#register} method, after which the
 * {@link #update(Host, Statement, Exception, long)} method will be called after each query of the driver to a Cassandra
 * host with the latency/duration (in nanoseconds) of this operation.
 */
public interface LatencyTracker {

    /**
     * A method that is called after each request to a Cassandra node with
     * the duration of that operation.
     * <p/>
     * Note that there is no guarantee that this method won't be called
     * concurrently by multiple threads, so implementations should synchronize
     * internally if need be.
     *
     * @param host            The Cassandra host on which a request has been performed.
     *                        This parameter is never {@code null}.
     * @param statement       The {@link com.datastax.driver.core.Statement} that has been executed.
     *                        This parameter is never {@code null}.
     * @param exception       An {@link Exception} thrown when receiving the response, or {@code null}
     *                        if the response was successful.
     * @param newLatencyNanos the latency in nanoseconds of the operation.
     *                        This latency corresponds to the time elapsed between
     *                        when the query was sent to {@code host} and
     *                        when the response was received by the driver
     *                        (or the operation timed out, in which {@code newLatencyNanos}
     *                        will approximately be the timeout value).
     */
    public void update(Host host, Statement statement, Exception exception, long newLatencyNanos);

    /**
     * Gets invoked when the tracker is registered with a cluster, or at cluster startup if the
     * tracker was registered at initialization with
     * {@link com.datastax.driver.core.Cluster.Initializer#register(LatencyTracker)}.
     *
     * @param cluster the cluster that this tracker is registered with.
     */
    void onRegister(Cluster cluster);

    /**
     * Gets invoked when the tracker is unregistered from a cluster, or at cluster shutdown if
     * the tracker was not unregistered.
     *
     * @param cluster the cluster that this tracker was registered with.
     */
    void onUnregister(Cluster cluster);
}
