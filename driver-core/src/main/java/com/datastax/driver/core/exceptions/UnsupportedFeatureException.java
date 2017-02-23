/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

import com.datastax.driver.core.ProtocolVersion;

/**
 * Exception thrown when a feature is not supported by the native protocol
 * currently in use.
 */
public class UnsupportedFeatureException extends DriverException {

    private static final long serialVersionUID = 0;

    private final ProtocolVersion currentVersion;

    public UnsupportedFeatureException(ProtocolVersion currentVersion, String msg) {
        super("Unsupported feature with the native protocol " + currentVersion + " (which is currently in use): " + msg);
        this.currentVersion = currentVersion;
    }

    public ProtocolVersion getCurrentVersion() {
        return currentVersion;
    }

}
