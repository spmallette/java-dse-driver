/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.geometry.codecs;

import com.datastax.driver.core.DataType;
import com.datastax.driver.dse.geometry.LineString;

import java.nio.ByteBuffer;

/**
 * A custom type codec to use {@link LineString} instances in the driver.
 * <p/>
 * If you use {@link com.datastax.driver.dse.DseCluster.Builder} to build your cluster, it will automatically register this codec.
 */
public class LineStringCodec extends GeometryCodec<LineString> {

    /**
     * The name of the server-side type handled by this codec.
     */
    public static final String CLASS_NAME = "org.apache.cassandra.db.marshal.LineStringType";

    /**
     * The datatype handled by this codec.
     */
    public static final DataType.CustomType DATA_TYPE = (DataType.CustomType) DataType.custom(CLASS_NAME);

    /**
     * The unique (stateless and thread-safe) instance of this codec.
     */
    public static final LineStringCodec INSTANCE = new LineStringCodec();

    private LineStringCodec() {
        super(DATA_TYPE, LineString.class);
    }

    @Override
    protected LineString fromWellKnownText(String source) {
        return LineString.fromWellKnownText(source);
    }

    @Override
    protected LineString fromWellKnownBinary(ByteBuffer source) {
        return LineString.fromWellKnownBinary(source);
    }

    @Override
    protected String toWellKnownText(LineString geometry) {
        return geometry.asWellKnownText();
    }

    @Override
    protected ByteBuffer toWellKnownBinary(LineString geometry) {
        return geometry.asWellKnownBinary();
    }

}
