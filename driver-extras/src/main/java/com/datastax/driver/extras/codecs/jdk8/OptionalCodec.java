/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.extras.codecs.jdk8;

import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.extras.codecs.MappingCodec;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

import java.util.Collection;
import java.util.Map;

/**
 * A codec that wraps other codecs around JDK 8's {@link java.util.Optional} API.
 *
 * @param <T> The wrapped Java type
 */
@IgnoreJDK6Requirement
@SuppressWarnings({"Since15", "OptionalUsedAsFieldOrParameterType"})
public class OptionalCodec<T> extends MappingCodec<java.util.Optional<T>, T> {

    private final java.util.function.Predicate<T> isAbsent;

    public OptionalCodec(TypeCodec<T> codec) {
        this(codec, new java.util.function.Predicate<T>() {
            @Override
            public boolean test(T input) {
                return input == null
                        || input instanceof Collection && ((Collection) input).isEmpty()
                        || input instanceof Map && ((Map) input).isEmpty();

            }
        });
    }

    public OptionalCodec(TypeCodec<T> codec, java.util.function.Predicate<T> isAbsent) {
        // @formatter:off
        super(codec, new TypeToken<java.util.Optional<T>>() {}.where(new TypeParameter<T>() {}, codec.getJavaType()));
        // @formatter:on
        this.isAbsent = isAbsent;
    }

    @Override
    protected java.util.Optional<T> deserialize(T value) {
        return isAbsent(value) ? java.util.Optional.<T>empty() : java.util.Optional.of(value);
    }

    @Override
    protected T serialize(java.util.Optional<T> value) {
        return value.isPresent() ? value.get() : absentValue();
    }

    protected T absentValue() {
        return null;
    }

    protected boolean isAbsent(T value) {
        return isAbsent.test(value);
    }

}
