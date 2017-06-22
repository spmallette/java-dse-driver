/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

/**
 * A value for a Tuple.
 */
public class TupleValue extends AbstractAddressableByIndexData<TupleValue> {

    private final TupleType type;

    /**
     * Builds a new value for a tuple.
     *
     * @param type the {@link TupleType} instance defining this tuple's components.
     */
    TupleValue(TupleType type) {
        super(type.getProtocolVersion(), type.getComponentTypes().size());
        this.type = type;
    }

    protected DataType getType(int i) {
        return type.getComponentTypes().get(i);
    }

    @Override
    protected String getName(int i) {
        // This is used for error messages
        return "component " + i;
    }

    @Override
    protected CodecRegistry getCodecRegistry() {
        return type.getCodecRegistry();
    }

    /**
     * The tuple type this is a value of.
     *
     * @return The tuple type this is a value of.
     */
    public TupleType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TupleValue))
            return false;

        TupleValue that = (TupleValue) o;
        if (!type.equals(that.type))
            return false;

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        TypeCodec<Object> codec = getCodecRegistry().codecFor(type);
        sb.append(codec.format(this));
        return sb.toString();
    }
}
