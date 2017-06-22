/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

import java.net.InetSocketAddress;

/**
 * A connection exception that has to do with the transport itself, i.e. that
 * suggests the node is down.
 */
public class TransportException extends ConnectionException {

    private static final long serialVersionUID = 0;

    public TransportException(InetSocketAddress address, String msg, Throwable cause) {
        super(address, msg, cause);
    }

    public TransportException(InetSocketAddress address, String msg) {
        super(address, msg);
    }

    @Override
    public TransportException copy() {
        return new TransportException(address, getRawMessage(), this);
    }

}
