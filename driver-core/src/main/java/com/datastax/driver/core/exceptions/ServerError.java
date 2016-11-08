/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Indicates that the contacted host reported an internal error.
 * This should be considered as a bug in Cassandra and reported as such.
 */
public class ServerError extends DriverInternalError implements CoordinatorException {

    private static final long serialVersionUID = 0;

    private final InetSocketAddress address;

    public ServerError(InetSocketAddress address, String message) {
        super(String.format("An unexpected error occurred server side on %s: %s", address, message));
        this.address = address;
    }

    /**
     * Private constructor used solely when copying exceptions.
     */
    private ServerError(InetSocketAddress address, String message, ServerError cause) {
        super(message, cause);
        this.address = address;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public InetAddress getHost() {
        return address.getAddress();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InetSocketAddress getAddress() {
        return address;
    }

    @Override
    public ServerError copy() {
        return new ServerError(address, getMessage(), this);
    }
}
