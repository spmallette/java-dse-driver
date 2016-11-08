/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.extras.codecs.arrays;

import com.datastax.driver.core.CodecUtils;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.exceptions.InvalidTypeException;

import java.lang.reflect.Array;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Codec dealing with Java object arrays.
 * Serialization and deserialization of elements in the array is
 * delegated to the provided element codec.
 * <p/>
 * For example, to create a codec that maps {@code list<text>} to {@code String[]},
 * declare the following:
 * <pre>{@code
 * ObjectArrayCodec<String> stringArrayCodec = new ObjectArrayCodec<>(
 *      DataType.list(DataType.varchar()),
 *      String[].class,
 *      TypeCodec.varchar());
 * }</pre>
 *
 * @param <E> The Java array component type this codec handles
 */
public class ObjectArrayCodec<E> extends AbstractArrayCodec<E[]> {

    protected final TypeCodec<E> eltCodec;

    public ObjectArrayCodec(DataType.CollectionType cqlType, Class<E[]> javaClass, TypeCodec<E> eltCodec) {
        super(cqlType, javaClass);
        this.eltCodec = eltCodec;
    }

    @Override
    public ByteBuffer serialize(E[] value, ProtocolVersion protocolVersion) {
        if (value == null)
            return null;
        int i = 0;
        ByteBuffer[] bbs = new ByteBuffer[value.length];
        for (E elt : value) {
            if (elt == null) {
                throw new NullPointerException("Collection elements cannot be null");
            }
            ByteBuffer bb;
            try {
                bb = eltCodec.serialize(elt, protocolVersion);
            } catch (ClassCastException e) {
                throw new InvalidTypeException(
                        String.format("Invalid type for %s element, expecting %s but got %s",
                                cqlType, eltCodec.getJavaType(), elt.getClass()), e);
            }
            bbs[i++] = bb;
        }
        return CodecUtils.pack(bbs, value.length, protocolVersion);
    }

    @Override
    public E[] deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) {
        if (bytes == null || bytes.remaining() == 0)
            return newInstance(0);
        try {
            ByteBuffer input = bytes.duplicate();
            int n = CodecUtils.readSize(input, protocolVersion);
            E[] array = newInstance(n);
            for (int i = 0; i < n; i++) {
                ByteBuffer databb = CodecUtils.readValue(input, protocolVersion);
                array[i] = eltCodec.deserialize(databb, protocolVersion);
            }
            return array;
        } catch (BufferUnderflowException e) {
            throw new InvalidTypeException("Not enough bytes to deserialize list");
        }
    }

    @Override
    protected void formatElement(StringBuilder output, E[] array, int index) {
        output.append(eltCodec.format(array[index]));
    }

    @Override
    protected void parseElement(String input, E[] array, int index) {
        array[index] = eltCodec.parse(input);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected E[] newInstance(int size) {
        return (E[]) Array.newInstance(getJavaType().getRawType().getComponentType(), size);
    }
}
