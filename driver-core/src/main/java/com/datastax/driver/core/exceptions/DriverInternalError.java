/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

/**
 * An unexpected error happened internally.
 * <p/>
 * This should never be raised and indicates a bug (either in the driver or in
 * Cassandra).
 */
public class DriverInternalError extends DriverException {

    private static final long serialVersionUID = 0;

    public DriverInternalError(String message) {
        super(message);
    }

    public DriverInternalError(Throwable cause) {
        super(cause);
    }

    public DriverInternalError(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public DriverInternalError copy() {
        return new DriverInternalError(getMessage(), this);
    }
}
