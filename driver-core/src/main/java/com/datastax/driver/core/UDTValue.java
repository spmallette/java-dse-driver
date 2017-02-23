/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

/**
 * A value for a User Defined Type.
 */
public class UDTValue extends AbstractData<UDTValue> {

    private final UserType definition;

    UDTValue(UserType definition) {
        super(definition.getProtocolVersion(), definition.size());
        this.definition = definition;
    }

    @Override
    protected DataType getType(int i) {
        return definition.byIdx[i].getType();
    }

    @Override
    protected String getName(int i) {
        return definition.byIdx[i].getName();
    }

    @Override
    protected CodecRegistry getCodecRegistry() {
        return definition.getCodecRegistry();
    }

    @Override
    protected int[] getAllIndexesOf(String name) {
        int[] indexes = definition.byName.get(Metadata.handleId(name));
        if (indexes == null)
            throw new IllegalArgumentException(name + " is not a field defined in this UDT");
        return indexes;
    }

    /**
     * The UDT this is a value of.
     *
     * @return the UDT this is a value of.
     */
    public UserType getType() {
        return definition;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UDTValue))
            return false;

        UDTValue that = (UDTValue) o;
        if (!definition.equals(that.definition))
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
        TypeCodec<Object> codec = getCodecRegistry().codecFor(definition);
        sb.append(codec.format(this));
        return sb.toString();
    }
}
