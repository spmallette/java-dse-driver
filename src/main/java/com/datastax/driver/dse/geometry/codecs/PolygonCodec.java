/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.geometry.codecs;

import com.datastax.driver.core.DataType;
import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.geometry.Polygon;

import java.nio.ByteBuffer;

/**
 * A custom type codec to use {@link Polygon} instances in the driver.
 * <p/>
 * If you use {@link DseCluster.Builder} to build your cluster, it will automatically register this codec.
 */
public class PolygonCodec extends GeometryCodec<Polygon> {

    /**
     * The name of the server-side type handled by this codec.
     */
    public static final String CLASS_NAME = "org.apache.cassandra.db.marshal.PolygonType";

    /**
     * The datatype handled by this codec.
     */
    public static final DataType.CustomType DATA_TYPE = (DataType.CustomType) DataType.custom(CLASS_NAME);

    /**
     * The unique (stateless and thread-safe) instance of this codec.
     */
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
