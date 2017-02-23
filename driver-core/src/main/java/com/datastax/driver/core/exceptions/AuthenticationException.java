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
 * Indicates an error during the authentication phase while connecting to a node.
 */
public class AuthenticationException extends DriverException implements CoordinatorException {

    private static final long serialVersionUID = 0;

    private final InetSocketAddress address;

    public AuthenticationException(InetSocketAddress address, String message) {
        super(String.format("Authentication error on host %s: %s", address, message));
        this.address = address;
    }

    private AuthenticationException(InetSocketAddress address, String message, Throwable cause) {
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
    public DriverException copy() {
        return new AuthenticationException(address, getMessage(), this);
    }
}
