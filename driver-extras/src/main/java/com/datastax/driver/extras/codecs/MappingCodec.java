/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.extras.codecs;

import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.google.common.reflect.TypeToken;

import java.nio.ByteBuffer;

/**
 * An abstract {@link TypeCodec} that maps a Java Pojo to another Java object
 * that can in turn be serialized into a CQL type.
 * This can serve as a base for libraries dealing with Pojo mappings.
 * <p/>
 * This codec can be seen as a convenience base class for libraries
 * dealing with Pojo-to-Pojo mappings, but it comes
 * with a performance penalty: each Java object is serialized
 * in two steps: first to an intermediate object, and then to a {@link ByteBuffer},
 * which means that each serialization actually incurs in two potentially
 * expensive operations being carried.
 * <p/>
 * If such operations are really expensive, and your mapping library is capable
 * of serializing objects directly to {@link ByteBuffer}s,
 * consider writing your own codec instead of using this one.
 *
 * @param <O> The "outer" (user-facing) Java type
 * @param <I> The "inner" (Cassandra-facing, intermediary) Java type
 */
public abstract class MappingCodec<O, I> extends TypeCodec<O> {

    protected final TypeCodec<I> innerCodec;

    public MappingCodec(TypeCodec<I> innerCodec, Class<O> javaType) {
        this(innerCodec, TypeToken.of(javaType));
    }

    public MappingCodec(TypeCodec<I> innerCodec, TypeToken<O> javaType) {
        super(innerCodec.getCqlType(), javaType);
        this.innerCodec = innerCodec;
    }

    @Override
    public ByteBuffer serialize(O value, ProtocolVersion protocolVersion) throws InvalidTypeException {
        return innerCodec.serialize(serialize(value), protocolVersion);
    }

    @Override
    public O deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) throws InvalidTypeException {
        return deserialize(innerCodec.deserialize(bytes, protocolVersion));
    }

    @Override
    public O parse(String value) throws InvalidTypeException {
        return value == null || value.isEmpty() || value.equalsIgnoreCase("NULL") ? null : deserialize(innerCodec.parse(value));
    }

    @Override
    public String format(O value) throws InvalidTypeException {
        return value == null ? null : innerCodec.format(serialize(value));
    }

    protected abstract O deserialize(I value);

    protected abstract I serialize(O value);

}
