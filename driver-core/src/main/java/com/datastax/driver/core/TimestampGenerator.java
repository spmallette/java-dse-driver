/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

/**
 * Generates client-side, microsecond-precision query timestamps.
 * <p/>
 * Given that Cassandra uses those timestamps to resolve conflicts, implementations should generate
 * monotonically increasing timestamps for successive invocations of {@link #next()}.
 */
public interface TimestampGenerator {

    /**
     * Returns the next timestamp.
     * <p/>
     * Implementors should enforce increasing monotonicity of timestamps, that is,
     * a timestamp returned should always be strictly greater that any previously returned
     * timestamp.
     * <p/>
     * Implementors should strive to achieve microsecond precision in the best possible way,
     * which is usually largely dependent on the underlying operating system's capabilities.
     *
     * @return the next timestamp (in microseconds). If it equals {@link Long#MIN_VALUE}, it won't be
     * sent by the driver, letting Cassandra generate a server-side timestamp.
     */
    long next();
}
