/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

/**
 * A timestamp generator that always returns {@link Long#MIN_VALUE}, in order to let Cassandra
 * assign server-side timestamps.
 */
public class ServerSideTimestampGenerator implements TimestampGenerator {
    /**
     * The unique instance of this generator.
     */
    public static final TimestampGenerator INSTANCE = new ServerSideTimestampGenerator();

    @Override
    public long next() {
        return Long.MIN_VALUE;
    }

    private ServerSideTimestampGenerator() {
    }
}
