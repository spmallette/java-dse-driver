/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.mapping;

/**
 * A strategy to determine how mapped properties are discovered,
 * and how to access them.
 */
public enum PropertyAccessStrategy {

    /**
     * Use getters and setters exclusively. These must be available for all mapped properties.
     */
    GETTERS_AND_SETTERS,

    /**
     * Use field access exclusively. Fields do not need to be declared public,
     * the driver will attempt to make them accessible via reflection if required.
     */
    FIELDS,

    /**
     * Use getters and setters preferably, and if these are not available,
     * use field access. Fields do not need to be declared public,
     * the driver will attempt to make them accessible via reflection if required.
     * This is the default access strategy.
     */
    BOTH;

    /**
     * Returns {@code true} if field scan is allowed, {@code false} otherwise.
     *
     * @return {@code true} if field access is allowed, {@code false} otherwise.
     */
    public boolean isFieldScanAllowed() {
        return this == FIELDS || this == BOTH;
    }

    /**
     * Returns {@code true} if getter and setter scan is allowed, {@code false} otherwise.
     *
     * @return {@code true} if getter and setter access is allowed, {@code false} otherwise.
     */
    public boolean isGetterSetterScanAllowed() {
        return this == GETTERS_AND_SETTERS || this == BOTH;
    }

}
