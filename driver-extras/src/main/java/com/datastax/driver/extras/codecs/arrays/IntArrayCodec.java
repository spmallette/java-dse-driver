/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.extras.codecs.arrays;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ProtocolVersion;

import java.nio.ByteBuffer;

/**
 * A codec that maps the CQL type {@code list<int>} to the Java type {@code int[]}.
 * <p/>
 * Note that this codec is designed for performance and converts CQL lists
 * <em>directly</em> to {@code int[]}, thus avoiding any unnecessary
 * boxing and unboxing of Java primitive {@code int} values;
 * it also instantiates arrays without the need for an intermediary
 * Java {@code List} object.
 */
public class IntArrayCodec extends AbstractPrimitiveArrayCodec<int[]> {

    public static final IntArrayCodec instance = new IntArrayCodec();

    public IntArrayCodec() {
        super(DataType.list(DataType.cint()), int[].class);
    }

    @Override
    protected int sizeOfComponentType() {
        return 4;
    }

    @Override
    protected void serializeElement(ByteBuffer output, int[] array, int index, ProtocolVersion protocolVersion) {
        output.putInt(array[index]);
    }

    @Override
    protected void deserializeElement(ByteBuffer input, int[] array, int index, ProtocolVersion protocolVersion) {
        array[index] = input.getInt();
    }

    @Override
    protected void formatElement(StringBuilder output, int[] array, int index) {
        output.append(array[index]);
    }

    @Override
    protected void parseElement(String input, int[] array, int index) {
        array[index] = Integer.parseInt(input);
    }

    @Override
    protected int[] newInstance(int size) {
        return new int[size];
    }

}
