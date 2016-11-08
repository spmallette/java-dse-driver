/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

/**
 * Indicates an error while deserializing a (previously serialized)
 * {@link com.datastax.driver.core.PagingState} object,
 * or when a paging state does not match the statement being executed.
 *
 * @see com.datastax.driver.core.PagingState
 */
public class PagingStateException extends DriverException {

    private static final long serialVersionUID = 0;

    public PagingStateException(String msg) {
        super(msg);
    }

    public PagingStateException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
