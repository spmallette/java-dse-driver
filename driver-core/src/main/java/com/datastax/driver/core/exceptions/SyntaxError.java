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
 * Indicates a syntax error in a query.
 */
public class SyntaxError extends QueryValidationException implements CoordinatorException {

    private static final long serialVersionUID = 0;

    private final InetSocketAddress address;

    public SyntaxError(InetSocketAddress address, String msg) {
        super(msg);
        this.address = address;
    }

    private SyntaxError(InetSocketAddress address, String msg, Throwable cause) {
        super(msg, cause);
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
    public SyntaxError copy() {
        return new SyntaxError(getAddress(), getMessage(), this);
    }
}
