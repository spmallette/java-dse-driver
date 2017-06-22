/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Indicates that the contacted host reported itself being overloaded.
 */
public class OverloadedException extends QueryExecutionException implements CoordinatorException {

    private static final long serialVersionUID = 0;

    private final InetSocketAddress address;

    public OverloadedException(InetSocketAddress address, String message) {
        super(String.format("Queried host (%s) was overloaded: %s", address, message));
        this.address = address;
    }

    /**
     * Private constructor used solely when copying exceptions.
     */
    private OverloadedException(InetSocketAddress address, String message, OverloadedException cause) {
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
    public OverloadedException copy() {
        return new OverloadedException(address, getMessage(), this);
    }
}
