/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
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
