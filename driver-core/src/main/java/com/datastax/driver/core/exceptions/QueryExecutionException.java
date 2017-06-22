/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

/**
 * Exception related to the execution of a query.
 * <p/>
 * This corresponds to the exception that Cassandra throws when a (valid) query
 * cannot be executed (TimeoutException, UnavailableException, ...).
 */
@SuppressWarnings("serial")
public abstract class QueryExecutionException extends DriverException {

    protected QueryExecutionException(String msg) {
        super(msg);
    }

    protected QueryExecutionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
