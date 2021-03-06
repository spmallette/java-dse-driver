/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.datastax.driver.core.exceptions.DriverInternalError;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import java.util.Map;

/**
 * Versions of the native protocol supported by the driver.
 */
public enum ProtocolVersion {

    V1("1.2.0", 1, null),
    V2("2.0.0", 2, V1),
    V3("2.1.0", 3, V2),
    V4("2.2.0", 4, V3),
    V5("3.10.0", 5, V4),
    DSE_V1("3.10.0", 65, V4);

    /**
     * The most recent protocol version supported by the driver.
     */
    public static final ProtocolVersion NEWEST_SUPPORTED = DSE_V1;

    /**
     * The most recent beta protocol version supported by the driver.
     */
    public static final ProtocolVersion NEWEST_BETA = V5;

    private final VersionNumber minCassandraVersion;

    private final int asInt;

    private final ProtocolVersion lowerSupported;

    private ProtocolVersion(String minCassandraVersion, int asInt, ProtocolVersion lowerSupported) {
        this.minCassandraVersion = VersionNumber.parse(minCassandraVersion);
        this.asInt = asInt;
        this.lowerSupported = lowerSupported;
    }

    VersionNumber minCassandraVersion() {
        return minCassandraVersion;
    }

    DriverInternalError unsupported() {
        return new DriverInternalError("Unsupported protocol version " + this);
    }

    /**
     * Returns the version as an integer.
     *
     * @return the integer representation.
     */
    public int toInt() {
        return asInt;
    }

    /**
     * Returns the highest supported version that is lower than this version.
     * Returns {@code null} if there isn't such a version.
     *
     * @return the highest supported version that is lower than this version.
     */
    public ProtocolVersion getLowerSupported() {
        return lowerSupported;
    }

    private static final Map<Integer, ProtocolVersion> INT_TO_VERSION;

    static {
        Builder<Integer, ProtocolVersion> builder = ImmutableMap.builder();
        for (ProtocolVersion version : values()) {
            builder.put(version.asInt, version);
        }
        INT_TO_VERSION = builder.build();
    }

    /**
     * Returns the value matching an integer version.
     *
     * @param i the version as an integer.
     * @return the matching enum value.
     * @throws IllegalArgumentException if the argument doesn't match any known version.
     */
    public static ProtocolVersion fromInt(int i) {
        ProtocolVersion version = INT_TO_VERSION.get(i);
        if (version == null)
            throw new IllegalArgumentException("No protocol version matching integer version " + i);
        return version;
    }
}
