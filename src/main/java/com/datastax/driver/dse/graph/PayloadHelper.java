/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import java.nio.ByteBuffer;

import static com.google.common.base.Charsets.UTF_8;

class PayloadHelper {
    static ByteBuffer asBytes(String s) {
        return ByteBuffer.wrap(s.getBytes(UTF_8));
    }
}
