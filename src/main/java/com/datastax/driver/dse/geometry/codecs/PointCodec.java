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
package com.datastax.driver.dse.geometry.codecs;

import com.datastax.driver.core.DataType;
import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.geometry.Point;

import java.nio.ByteBuffer;

/**
 * A custom type codec to use {@link Point} instances in the driver.
 * <p/>
 * If you use {@link DseCluster.Builder} to build your cluster, it will automatically register this codec.
 */
public class PointCodec extends GeometryCodec<Point> {

    /**
     * The name of the server-side type handled by this codec.
     */
    public static final String CLASS_NAME = "org.apache.cassandra.db.marshal.PointType";

    /**
     * The datatype handled by this codec.
     */
    public static final DataType.CustomType DATA_TYPE = (DataType.CustomType) DataType.custom(CLASS_NAME);

    /**
     * The unique (stateless and thread-safe) instance of this codec.
     */
    public static final PointCodec INSTANCE = new PointCodec();

    private PointCodec() {
        super(DATA_TYPE, Point.class);
    }

    @Override
    protected String toWellKnownText(Point geometry) {
        return geometry.asWellKnownText();
    }

    @Override
    protected ByteBuffer toWellKnownBinary(Point geometry) {
        return geometry.asWellKnownBinary();
    }

    @Override
    protected Point fromWellKnownText(String source) {
        return Point.fromWellKnownText(source);
    }

    @Override
    protected Point fromWellKnownBinary(ByteBuffer source) {
        return Point.fromWellKnownBinary(source);
    }

}
