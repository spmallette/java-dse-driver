/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

import com.datastax.driver.core.ProtocolVersion;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Indicates that we've attempted to connect to a Cassandra node with a protocol version
 * that it cannot handle (e.g., connecting to a C* 1.2 node with protocol version 2).
 */
public class UnsupportedProtocolVersionException extends DriverException implements CoordinatorException {

    private static final long serialVersionUID = 0;

    private final InetSocketAddress address;

    private final ProtocolVersion unsupportedVersion;

    private final ProtocolVersion serverVersion;

    public UnsupportedProtocolVersionException(InetSocketAddress address, ProtocolVersion unsupportedVersion, ProtocolVersion serverVersion) {
        super(String.format("[%s] Host does not support protocol version %s but %s", address, unsupportedVersion, serverVersion));
        this.address = address;
        this.unsupportedVersion = unsupportedVersion;
        this.serverVersion = serverVersion;
    }

    public UnsupportedProtocolVersionException(InetSocketAddress address, ProtocolVersion unsupportedVersion, ProtocolVersion serverVersion, Throwable cause) {
        super(String.format("[%s] Host does not support protocol version %s but %s", address, unsupportedVersion, serverVersion), cause);
        this.address = address;
        this.unsupportedVersion = unsupportedVersion;
        this.serverVersion = serverVersion;
    }

    @Override
    public InetAddress getHost() {
        return address.getAddress();
    }

    @Override
    public InetSocketAddress getAddress() {
        return address;
    }

    public ProtocolVersion getServerVersion() {
        return serverVersion;
    }

    public ProtocolVersion getUnsupportedVersion() {
        return unsupportedVersion;
    }

    @Override
    public UnsupportedProtocolVersionException copy() {
        return new UnsupportedProtocolVersionException(address, unsupportedVersion, serverVersion, this);
    }


}
