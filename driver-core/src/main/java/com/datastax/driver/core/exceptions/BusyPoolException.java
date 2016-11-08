/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.Statement;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Indicates that a connection pool has run out of available connections.
 * <p/>
 * This happens if the pool has no connections (for example if it's currently reconnecting to its host), or if all
 * connections have reached their maximum number of in flight queries. The query will be retried on the next host in the
 * {@link com.datastax.driver.core.policies.LoadBalancingPolicy#newQueryPlan(String, Statement) query plan}.
 * <p/>
 * This exception is a symptom that the driver is experiencing a high workload. If it happens regularly on all hosts,
 * you should consider tuning one (or a combination of) the following pooling options:
 * <ul>
 * <li>{@link com.datastax.driver.core.PoolingOptions#setMaxRequestsPerConnection(HostDistance, int)}: maximum number of
 * requests per connection;</li>
 * <li>{@link com.datastax.driver.core.PoolingOptions#setMaxConnectionsPerHost(HostDistance, int)}: maximum number of
 * connections in the pool;</li>
 * <li>{@link com.datastax.driver.core.PoolingOptions#setMaxQueueSize(int)}: maximum number of enqueued requests before
 * this exception is thrown.</li>
 * </ul>
 */
public class BusyPoolException extends DriverException implements CoordinatorException {

    private static final long serialVersionUID = 0;

    private final InetSocketAddress address;
    private final int queueSize;

    public BusyPoolException(InetSocketAddress address, int queueSize) {
        this(address, queueSize, null);
    }

    private BusyPoolException(InetSocketAddress address, int queueSize, Throwable cause) {
        super(buildMessage(address, queueSize), cause);
        this.address = address;
        this.queueSize = queueSize;
    }

    private static String buildMessage(InetSocketAddress address, int queueSize) {
        return String.format("[%s] Pool is busy (no available connection and the queue has reached its max size %d)",
                address.getAddress(),
                queueSize);
    }

    @Override
    public InetAddress getHost() {
        return address.getAddress();
    }

    @Override
    public InetSocketAddress getAddress() {
        return address;
    }

    @Override
    public BusyPoolException copy() {
        return new BusyPoolException(address, queueSize, this);
    }

}
