/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.extras.codecs.arrays;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ProtocolVersion;

import java.nio.ByteBuffer;

/**
 * A codec that maps the CQL type {@code list<float>} to the Java type {@code float[]}.
 * <p/>
 * Note that this codec is designed for performance and converts CQL lists
 * <em>directly</em> to {@code float[]}, thus avoiding any unnecessary
 * boxing and unboxing of Java primitive {@code float} values;
 * it also instantiates arrays without the need for an intermediary
 * Java {@code List} object.
 */
public class FloatArrayCodec extends AbstractPrimitiveArrayCodec<float[]> {

    public static final FloatArrayCodec instance = new FloatArrayCodec();

    public FloatArrayCodec() {
        super(DataType.list(DataType.cfloat()), float[].class);
    }

    @Override
    protected int sizeOfComponentType() {
        return 4;
    }

    @Override
    protected void serializeElement(ByteBuffer output, float[] array, int index, ProtocolVersion protocolVersion) {
        output.putFloat(array[index]);
    }

    @Override
    protected void deserializeElement(ByteBuffer input, float[] array, int index, ProtocolVersion protocolVersion) {
        array[index] = input.getFloat();
    }

    @Override
    protected void formatElement(StringBuilder output, float[] array, int index) {
        output.append(array[index]);
    }

    @Override
    protected void parseElement(String input, float[] array, int index) {
        array[index] = Float.parseFloat(input);
    }

    @Override
    protected float[] newInstance(int size) {
        return new float[size];
    }

}
