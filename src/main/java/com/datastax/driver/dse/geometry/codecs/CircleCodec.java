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
import com.datastax.driver.dse.geometry.Circle;

import java.nio.ByteBuffer;

/**
 * A custom type codec to use {@link Circle} instances in the driver.
 * <p/>
 * If you use {@link DseCluster.Builder} to build your cluster, it will automatically register this codec.
 */
public class CircleCodec extends GeometryCodec<Circle> {

    /**
     * The name of the server-side type handled by this codec.
     */
    public static final String CLASS_NAME = "org.apache.cassandra.db.marshal.CircleType";

    /**
     * The datatype handled by this codec.
     */
    public static final DataType.CustomType DATA_TYPE = (DataType.CustomType) DataType.custom(CLASS_NAME);

    /**
     * The unique (stateless and thread-safe) instance of this codec.
     */
    public static final CircleCodec INSTANCE = new CircleCodec();

    private CircleCodec() {
        super(DATA_TYPE, Circle.class);
    }

    @Override
    protected Circle fromWellKnownText(String source) {
        return Circle.fromWellKnownText(source);
    }

    @Override
    protected Circle fromWellKnownBinary(ByteBuffer source) {
        return Circle.fromWellKnownBinary(source);
    }

    @Override
    protected String toWellKnownText(Circle geometry) {
        return geometry.asWellKnownText();
    }

    @Override
    protected ByteBuffer toWellKnownBinary(Circle geometry) {
        return geometry.asWellKnownBinary();
    }

}
