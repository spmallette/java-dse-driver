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
 * Indicates that the contacted host was bootstrapping when it received a read query.
 */
public class BootstrappingException extends QueryExecutionException implements CoordinatorException {

    private static final long serialVersionUID = 0;

    private final InetSocketAddress address;

    public BootstrappingException(InetSocketAddress address, String message) {
        super(String.format("Queried host (%s) was bootstrapping: %s", address, message));
        this.address = address;
    }

    /**
     * Private constructor used solely when copying exceptions.
     */
    private BootstrappingException(InetSocketAddress address, String message, BootstrappingException cause) {
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
    public BootstrappingException copy() {
        return new BootstrappingException(address, getMessage(), this);
    }
}
