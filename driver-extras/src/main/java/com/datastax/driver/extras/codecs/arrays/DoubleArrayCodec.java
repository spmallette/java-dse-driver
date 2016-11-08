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
 * A codec that maps the CQL type {@code list<double>} to the Java type {@code double[]}.
 * <p/>
 * Note that this codec is designed for performance and converts CQL lists
 * <em>directly</em> to {@code double[]}, thus avoiding any unnecessary
 * boxing and unboxing of Java primitive {@code double} values;
 * it also instantiates arrays without the need for an intermediary
 * Java {@code List} object.
 */
public class DoubleArrayCodec extends AbstractPrimitiveArrayCodec<double[]> {

    public static final DoubleArrayCodec instance = new DoubleArrayCodec();

    public DoubleArrayCodec() {
        super(DataType.list(DataType.cdouble()), double[].class);
    }

    @Override
    protected int sizeOfComponentType() {
        return 8;
    }

    @Override
    protected void serializeElement(ByteBuffer output, double[] array, int index, ProtocolVersion protocolVersion) {
        output.putDouble(array[index]);
    }

    @Override
    protected void deserializeElement(ByteBuffer input, double[] array, int index, ProtocolVersion protocolVersion) {
        array[index] = input.getDouble();
    }

    @Override
    protected void formatElement(StringBuilder output, double[] array, int index) {
        output.append(array[index]);
    }

    @Override
    protected void parseElement(String input, double[] array, int index) {
        array[index] = Double.parseDouble(input);
    }

    @Override
    protected double[] newInstance(int size) {
        return new double[size];
    }

}
