/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

/**
 * Thrown when a user type cannot be resolved.
 * <p/>
 * This exception can be raised when the driver is rebuilding
 * its schema metadata, and a user-defined type cannot be completely
 * constructed due to some missing information.
 * It should only appear in the driver logs, never in client code.
 * It shouldn't be considered as a severe error as long as it only
 * appears occasionally.
 */
public class UnresolvedUserTypeException extends DriverException {

    private final String keyspaceName;

    private final String name;

    public UnresolvedUserTypeException(String keyspaceName, String name) {
        super(String.format("Cannot resolve user type %s.%s", keyspaceName, name));
        this.keyspaceName = keyspaceName;
        this.name = name;
    }

    private UnresolvedUserTypeException(String keyspaceName, String name, Throwable cause) {
        super(String.format("Cannot resolve user type %s.%s", keyspaceName, name), cause);
        this.keyspaceName = keyspaceName;
        this.name = name;
    }

    public String getKeyspaceName() {
        return keyspaceName;
    }

    public String getName() {
        return name;
    }

    @Override
    public UnresolvedUserTypeException copy() {
        return new UnresolvedUserTypeException(keyspaceName, name, this);
    }

}
