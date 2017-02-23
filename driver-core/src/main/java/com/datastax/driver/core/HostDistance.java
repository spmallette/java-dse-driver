/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

/**
 * The distance to a Cassandra node as assigned by a
 * {@link com.datastax.driver.core.policies.LoadBalancingPolicy} (through its {@code
 * distance} method).
 * <p/>
 * The distance assigned to an host influences how many connections the driver
 * maintains towards this host. If for a given host the assigned {@code HostDistance}
 * is {@code LOCAL} or {@code REMOTE}, some connections will be maintained by
 * the driver to this host. More active connections will be kept to
 * {@code LOCAL} host than to a {@code REMOTE} one (and thus well behaving
 * {@code LoadBalancingPolicy} should assign a {@code REMOTE} distance only to
 * hosts that are the less often queried).
 * <p/>
 * However, if a host is assigned the distance {@code IGNORED}, no connection
 * to that host will maintained active. In other words, {@code IGNORED} should
 * be assigned to hosts that should not be used by this driver (because they
 * are in a remote data center for instance).
 */
public enum HostDistance {
    // Note: PoolingOptions rely on the order of the enum.
    LOCAL,
    REMOTE,
    IGNORED
}
