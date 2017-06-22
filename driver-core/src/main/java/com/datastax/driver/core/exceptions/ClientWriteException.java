/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

/**
 * An error occurred server side when sending asynchronous results to the client (us), i.e.
 * during a continuous paging session if we were too slow to read.
 */
public class ClientWriteException extends QueryExecutionException {

    public ClientWriteException(String msg) {
        super(msg);
    }
}
