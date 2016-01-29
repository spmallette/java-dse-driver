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
import com.datastax.driver.geometry.Polygon;

import java.nio.ByteBuffer;

public class PolygonCodec extends GeometryCodec<Polygon> {

    public static final String CLASS_NAME = "org.apache.cassandra.db.marshal.PolygonType";

    public static final DataType.CustomType DATA_TYPE = (DataType.CustomType) DataType.custom(CLASS_NAME);

    public static final PolygonCodec INSTANCE = new PolygonCodec();

    private PolygonCodec() {
        super(DATA_TYPE, Polygon.class);
    }

    @Override
    protected String toWellKnownText(Polygon geometry) {
        return geometry.asWellKnownText();
    }

    @Override
    protected ByteBuffer toWellKnownBinary(Polygon geometry) {
        return geometry.asWellKnownBinary();
    }

    @Override
    protected Polygon fromWellKnownText(String source) {
        return Polygon.fromWellKnownText(source);
    }

    @Override
    protected Polygon fromWellKnownBinary(ByteBuffer source) {
        return Polygon.fromWellKnownBinary(source);
    }

}
