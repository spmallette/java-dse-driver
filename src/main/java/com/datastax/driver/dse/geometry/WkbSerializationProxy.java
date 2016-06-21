/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.geometry;

import com.datastax.driver.core.utils.Bytes;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A thin wrapper around a Well-Known Binary byte sequence, that gets substituted for {@link Geometry} instances during
 * the serialization / deserialization process. This allows immutable geometry classes.
 */
class WkbSerializationProxy implements Serializable {

    private static final long serialVersionUID = 1L;

    private final byte[] wkb;

    WkbSerializationProxy(ByteBuffer wkb) {
        this.wkb = Bytes.getArray(wkb);
    }

    private Object readResolve() {
        ByteBuffer buffer = ByteBuffer.wrap(wkb).order(ByteOrder.nativeOrder());
        int type = buffer.getInt(1);

        if (type == 1)
            return Point.fromWellKnownBinary(buffer);
        else if (type == 2)
            return LineString.fromWellKnownBinary(buffer);
        else if (type == 3)
            return Polygon.fromWellKnownBinary(buffer);
        else
            throw new IllegalArgumentException("Unknown geospatial type code in serialized form: " + type);
    }
}
