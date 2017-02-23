/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Indicates that the contacted host reported a protocol error.
 * Protocol errors indicate that the client triggered a protocol
 * violation (for instance, a QUERY message is sent before a STARTUP one has been sent).
 * Protocol errors should be considered as a bug in the driver and reported as such.
 */
public class ProtocolError extends DriverInternalError implements CoordinatorException {

    private static final long serialVersionUID = 0;

    private final InetSocketAddress address;

    public ProtocolError(InetSocketAddress address, String message) {
        super(String.format("An unexpected protocol error occurred on host %s. This is a bug in this library, please report: %s", address, message));
        this.address = address;
    }

    /**
     * Private constructor used solely when copying exceptions.
     */
    private ProtocolError(InetSocketAddress address, String message, ProtocolError cause) {
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
    public ProtocolError copy() {
        return new ProtocolError(address, getMessage(), this);
    }
}
