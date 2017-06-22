/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

public class CCMException extends RuntimeException {

    private final String out;

    public CCMException(String message, String out) {
        super(message);
        this.out = out;
    }

    public CCMException(String message, String out, Throwable cause) {
        super(message, cause);
        this.out = out;
    }

    public String getOut() {
        return out;
    }

}
