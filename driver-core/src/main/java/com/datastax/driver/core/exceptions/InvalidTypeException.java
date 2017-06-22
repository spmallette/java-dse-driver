/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

/**
 * Thrown when a {@link com.datastax.driver.core.TypeCodec}
 * is unable to perform the requested operation (serialization,
 * deserialization, parsing or formatting) because the
 * object or the byte buffer content being processed does not
 * comply with the expected Java and/or CQL type.
 */
public class InvalidTypeException extends DriverException {

    private static final long serialVersionUID = 0;

    public InvalidTypeException(String msg) {
        super(msg);
    }

    public InvalidTypeException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public InvalidTypeException copy() {
        return new InvalidTypeException(getMessage(), this);
    }
}
