/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

/**
 * Exception thrown if a query trace cannot be retrieved.
 *
 * @see com.datastax.driver.core.QueryTrace
 */
public class TraceRetrievalException extends DriverException {

    private static final long serialVersionUID = 0;

    public TraceRetrievalException(String message) {
        super(message);
    }

    public TraceRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public TraceRetrievalException copy() {
        return new TraceRetrievalException(getMessage(), this);
    }
}
