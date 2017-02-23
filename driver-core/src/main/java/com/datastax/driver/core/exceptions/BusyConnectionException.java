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
 * Indicates that a connection has run out of stream IDs.
 */
public class BusyConnectionException extends DriverException implements CoordinatorException {

    private static final long serialVersionUID = 0;

    private final InetSocketAddress address;

    public BusyConnectionException(InetSocketAddress address) {
        super(String.format("[%s] Connection has run out of stream IDs", address.getAddress()));
        this.address = address;
    }

    public BusyConnectionException(InetSocketAddress address, Throwable cause) {
        super(String.format("[%s] Connection has run out of stream IDs", address.getAddress()), cause);
        this.address = address;
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
    public BusyConnectionException copy() {
        return new BusyConnectionException(address, this);
    }

}
