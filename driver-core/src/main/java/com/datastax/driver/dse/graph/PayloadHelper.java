/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;

import java.nio.ByteBuffer;

class PayloadHelper {
    static ByteBuffer asBytes(String s) {
        return TypeCodec.varchar().serialize(s, ProtocolVersion.NEWEST_SUPPORTED);
    }

    static ByteBuffer asBytes(String s, ProtocolVersion protocolVersion) {
        return TypeCodec.varchar().serialize(s, protocolVersion);
    }

    static ByteBuffer asBytes(long l, ProtocolVersion protocolVersion) {
        return TypeCodec.bigint().serializeNoBoxing(l, protocolVersion);
    }
}
