/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.google.common.reflect.TypeToken;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;

public abstract class AbstractGettableData extends AbstractGettableByIndexData implements GettableData {

    /**
     * Creates a new AbstractGettableData object.
     *
     * @param protocolVersion the protocol version in which values returned
     *                        by {@link #getValue} will be returned. This must be a protocol version
     *                        supported by this driver. In general, the correct value will be the
     *                        value returned by {@link ProtocolOptions#getProtocolVersion}.
     * @throws IllegalArgumentException if {@code protocolVersion} is not a valid protocol version.
     */
    protected AbstractGettableData(ProtocolVersion protocolVersion) {
        super(protocolVersion);
    }

    /**
     * Returns the index corresponding to a given name.
     *
     * @param name the name for which to return the index of.
     * @return the index for the value coressponding to {@code name}.
     * @throws IllegalArgumentException if {@code name} is not valid name for this object.
     */
    protected abstract int getIndexOf(String name);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNull(String name) {
        return isNull(getIndexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getBool(String name) {
        return getBool(getIndexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte getByte(String name) {
        return getByte(getIndexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getShort(String name) {
        return getShort(getIndexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInt(String name) {
        return getInt(getIndexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLong(String name) {
        return getLong(getIndexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getTimestamp(String name) {
        return getTimestamp(getIndexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDate getDate(String name) {
        return getDate(getIndexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTime(String name) {
        return getTime(getIndexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getFloat(String name) {
        return getFloat(getIndexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getDouble(String name) {
        return getDouble(getIndexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ByteBuffer getBytesUnsafe(String name) {
        return getBytesUnsafe(getIndexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ByteBuffer getBytes(String name) {
        return getBytes(getIndexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getString(String name) {
        return getString(getIndexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigInteger getVarint(String name) {
        return getVarint(getIndexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getDecimal(String name) {
        return getDecimal(getIndexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getUUID(String name) {
        return getUUID(getIndexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InetAddress getInet(String name) {
        return getInet(getIndexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<T> getList(String name, Class<T> elementsClass) {
        return getList(getIndexOf(name), elementsClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<T> getList(String name, TypeToken<T> elementsType) {
        return getList(getIndexOf(name), elementsType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Set<T> getSet(String name, Class<T> elementsClass) {
        return getSet(getIndexOf(name), elementsClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Set<T> getSet(String name, TypeToken<T> elementsType) {
        return getSet(getIndexOf(name), elementsType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Map<K, V> getMap(String name, Class<K> keysClass, Class<V> valuesClass) {
        return getMap(getIndexOf(name), keysClass, valuesClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Map<K, V> getMap(String name, TypeToken<K> keysType, TypeToken<V> valuesType) {
        return getMap(getIndexOf(name), keysType, valuesType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UDTValue getUDTValue(String name) {
        return getUDTValue(getIndexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TupleValue getTupleValue(String name) {
        return getTupleValue(getIndexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getObject(String name) {
        return getObject(getIndexOf(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(String name, Class<T> targetClass) {
        return get(getIndexOf(name), targetClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(String name, TypeToken<T> targetType) {
        return get(getIndexOf(name), targetType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(String name, TypeCodec<T> codec) {
        return get(getIndexOf(name), codec);
    }
}
