/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.geometry.codecs;

import com.datastax.driver.core.DataType;
import com.datastax.driver.dse.geometry.Point;

import java.nio.ByteBuffer;

/**
 * A custom type codec to use {@link Point} instances in the driver.
 * <p/>
 * If you use {@link com.datastax.driver.dse.DseCluster.Builder} to build your cluster, it will automatically register this codec.
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
