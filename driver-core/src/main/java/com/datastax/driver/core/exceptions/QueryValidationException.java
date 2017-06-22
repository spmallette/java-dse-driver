/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

/**
 * An exception indicating that a query cannot be executed because it is
 * syntactically incorrect, invalid, unauthorized or any other reason.
 */
@SuppressWarnings("serial")
public abstract class QueryValidationException extends DriverException {

    protected QueryValidationException(String msg) {
        super(msg);
    }

    protected QueryValidationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
