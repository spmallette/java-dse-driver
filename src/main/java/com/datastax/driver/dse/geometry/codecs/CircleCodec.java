/*
 *      Copyright (C) 2012-2016 DataStax Inc.
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
