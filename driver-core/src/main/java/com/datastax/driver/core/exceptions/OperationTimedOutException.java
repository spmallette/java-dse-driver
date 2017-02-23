/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

import com.datastax.driver.core.SocketOptions;

import java.net.InetSocketAddress;

/**
 * Thrown on a client-side timeout, i.e. when the client didn't hear back from the server within
 * {@link SocketOptions#getReadTimeoutMillis()}.
 */
public class OperationTimedOutException extends ConnectionException {

    private static final long serialVersionUID = 0;

    public OperationTimedOutException(InetSocketAddress address) {
        super(address, "Operation timed out");
    }

    public OperationTimedOutException(InetSocketAddress address, String msg) {
        super(address, msg);
    }

    public OperationTimedOutException(InetSocketAddress address, String msg, Throwable cause) {
        super(address, msg, cause);
    }

    @Override
    public OperationTimedOutException copy() {
        return new OperationTimedOutException(address, getRawMessage(), this);
    }

}
