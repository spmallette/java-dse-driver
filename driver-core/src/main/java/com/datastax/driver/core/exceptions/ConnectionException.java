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
 * Indicates that a connection to a host has encountered a problem
 * and that it should be closed.
 */
public class ConnectionException extends DriverException implements CoordinatorException {

    private static final long serialVersionUID = 0;

    public final InetSocketAddress address;

    public ConnectionException(InetSocketAddress address, String msg, Throwable cause) {
        super(msg, cause);
        this.address = address;
    }

    public ConnectionException(InetSocketAddress address, String msg) {
        super(msg);
        this.address = address;
    }

    @Override
    public InetAddress getHost() {
        return address == null ? null : address.getAddress();
    }

    @Override
    public InetSocketAddress getAddress() {
        return address;
    }

    @Override
    public String getMessage() {
        return address == null ? getRawMessage() : String.format("[%s] %s", address, getRawMessage());
    }

    @Override
    public ConnectionException copy() {
        return new ConnectionException(address, getRawMessage(), this);
    }

    String getRawMessage() {
        return super.getMessage();
    }

}
