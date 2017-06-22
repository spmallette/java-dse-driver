/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

import com.datastax.driver.core.DataType;
import com.google.common.reflect.TypeToken;

/**
 * Thrown when a suitable {@link com.datastax.driver.core.TypeCodec} cannot be found by
 * {@link com.datastax.driver.core.CodecRegistry} instances.
 */
@SuppressWarnings("serial")
public class CodecNotFoundException extends DriverException {

    private final DataType cqlType;

    private final TypeToken<?> javaType;

    public CodecNotFoundException(String msg, DataType cqlType, TypeToken<?> javaType) {
        this(msg, null, cqlType, javaType);
    }

    public CodecNotFoundException(Throwable cause, DataType cqlType, TypeToken<?> javaType) {
        this(null, cause, cqlType, javaType);
    }

    private CodecNotFoundException(String msg, Throwable cause, DataType cqlType, TypeToken<?>javaType) {
        super(msg, cause);
        this.cqlType = cqlType;
        this.javaType = javaType;
    }

    public DataType getCqlType() {
        return cqlType;
    }

    public TypeToken<?> getJavaType() {
        return javaType;
    }

    @Override
    public CodecNotFoundException copy() {
        return new CodecNotFoundException(getMessage(), getCause(), getCqlType(), getJavaType());
    }
}
