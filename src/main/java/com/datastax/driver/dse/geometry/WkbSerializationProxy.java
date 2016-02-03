/*
 *      Copyright (C) 2012-2015 DataStax Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
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
        else if (type == 101)
            return Circle.fromWellKnownBinary(buffer);
        else
            throw new IllegalArgumentException("Unknown geospatial type code in serialized form: " + type);
    }
}
