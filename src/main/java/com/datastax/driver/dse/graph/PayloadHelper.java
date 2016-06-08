/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;

import java.nio.ByteBuffer;

import static com.google.common.base.Charsets.UTF_8;

class PayloadHelper {
    static ByteBuffer asBytes(String s) {
        return TypeCodec.varchar().serialize(s, ProtocolVersion.NEWEST_SUPPORTED);
    }

    static ByteBuffer asBytes(long l) {
        return TypeCodec.bigint().serializeNoBoxing(l, ProtocolVersion.NEWEST_SUPPORTED);
    }
}
