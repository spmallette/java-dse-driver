/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.schemabuilder;

import com.datastax.driver.core.DataType;

/**
 * Represents a native CQL type in a SchemaBuilder statement.
 */
class NativeColumnType implements ColumnType {
    private final String asCQLString;

    NativeColumnType(DataType nativeType) {
        asCQLString = nativeType.toString();
    }

    @Override
    public String asCQLString() {
        return asCQLString;
    }
}
