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
 * Indicates a syntactically correct but invalid query.
 */
public class InvalidQueryException extends QueryValidationException implements CoordinatorException {

    private static final long serialVersionUID = 0;

    private final InetSocketAddress address;

    public InvalidQueryException(String msg) {
        this(null, msg);
    }

    public InvalidQueryException(InetSocketAddress address, String msg) {
        super(msg);
        this.address = address;
    }

    public InvalidQueryException(String msg, Throwable cause) {
        this(null, msg, cause);
    }

    public InvalidQueryException(InetSocketAddress address, String msg, Throwable cause) {
        super(msg, cause);
        this.address = address;
    }

    @Override
    public DriverException copy() {
        return new InvalidQueryException(getAddress(), getMessage(), this);
    }

    @Override
    public InetAddress getHost() {
        return address != null ? address.getAddress() : null;
    }

    @Override
    public InetSocketAddress getAddress() {
        return address;
    }
}
