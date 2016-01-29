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
package com.datastax.driver.geometry.codecs;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.datastax.driver.geometry.Geometry;

import java.nio.ByteBuffer;

import static com.datastax.driver.core.ParseUtils.*;

/**
 * Base class for geospatial type codecs.
 */
abstract class GeometryCodec<T extends Geometry> extends TypeCodec<T> {

    protected GeometryCodec(DataType.CustomType cqlType, Class<T> javaType) {
        super(cqlType, javaType);
    }

    @Override
    public T deserialize(ByteBuffer bb, ProtocolVersion protocolVersion) throws InvalidTypeException {
        return bb == null || bb.remaining() == 0 ? null : fromWellKnownBinary(bb.slice());
    }

    @Override
    public ByteBuffer serialize(T geometry, ProtocolVersion protocolVersion) throws InvalidTypeException {
        return geometry == null ? null : toWellKnownBinary(geometry);
    }

    @Override
    public T parse(String s) throws InvalidTypeException {
        if (s == null)
            return null;
        s = s.trim();
        if (s.isEmpty() || s.equalsIgnoreCase("NULL"))
            return null;
        if (!isQuoted(s))
            throw new InvalidTypeException("Geometry values must be enclosed by single quotes");
        return fromWellKnownText(unquote(s));
    }

    @Override
    public String format(T geometry) throws InvalidTypeException {
        return geometry == null ? "NULL" : quote(toWellKnownText(geometry));
    }

    /**
     * Creates an instance of this codec's geospatial type from
     * its <a href="https://en.wikipedia.org/wiki/Well-known_text">Well-known Text</a> (WKT)
     * representation.
     *
     * @param source the Well-known Text representation to parse. Cannot be null.
     * @return A new instance of this codec's geospatial type.
     * @throws InvalidTypeException if the string does not contain a valid Well-known Text representation.
     */
    protected abstract T fromWellKnownText(String source);

    /**
     * Creates an instance of a geospatial type from
     * its <a href="https://en.wikipedia.org/wiki/Well-known_text#Well-known_binary">Well-known Binary</a> (WKB)
     * representation.
     *
     * @param bb the Well-known Binary representation to parse. Cannot be null.
     * @return A new instance of this codec's geospatial type.
     * @throws InvalidTypeException if the given {@link ByteBuffer} does not contain a valid Well-known Binary representation.
     */
    protected abstract T fromWellKnownBinary(ByteBuffer bb);

    /**
     * Returns a <a href="https://en.wikipedia.org/wiki/Well-known_text">Well-known Text</a> (WKT)
     * representation of the given geospatial object.
     *
     * @param geometry the geospatial object to convert. Cannot be null.
     * @return A Well-known Text representation of the given object.
     */
    protected abstract String toWellKnownText(T geometry);

    /**
     * Returns a <a href="https://en.wikipedia.org/wiki/Well-known_text#Well-known_binary">Well-known Binary</a> (WKB)
     * representation of the given geospatial object.
     *
     * @param geometry the geospatial object to convert. Cannot be null.
     * @return A Well-known Binary representation of the given object.
     */
    protected abstract ByteBuffer toWellKnownBinary(T geometry);

}
