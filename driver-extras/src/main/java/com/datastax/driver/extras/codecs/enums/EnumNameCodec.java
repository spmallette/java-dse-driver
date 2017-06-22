/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.extras.codecs.enums;

import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.extras.codecs.ParsingCodec;

/**
 * A codec that serializes {@link Enum} instances as CQL {@code varchar}s
 * representing their programmatic names as returned by {@link Enum#name()}.
 * <p/>
 * <strong>Note that this codec relies on the enum constant names;
 * it is therefore vital that enum names never change.</strong>
 *
 * @param <E> The Enum class this codec serializes from and deserializes to.
 */
public class EnumNameCodec<E extends Enum<E>> extends ParsingCodec<E> {

    private final Class<E> enumClass;

    public EnumNameCodec(Class<E> enumClass) {
        this(TypeCodec.varchar(), enumClass);
    }

    public EnumNameCodec(TypeCodec<String> innerCodec, Class<E> enumClass) {
        super(innerCodec, enumClass);
        this.enumClass = enumClass;
    }

    @Override
    protected String toString(E value) {
        return value == null ? null : value.name();
    }

    @Override
    protected E fromString(String value) {
        return value == null ? null : Enum.valueOf(enumClass, value);
    }

}
