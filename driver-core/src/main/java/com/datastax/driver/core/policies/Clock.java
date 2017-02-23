/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.policies;

/**
 * Wraps System.nanoTime() to make it easy to mock in tests.
 */
class Clock {
    static final Clock DEFAULT = new Clock();

    long nanoTime() {
        return System.nanoTime();
    }
}
