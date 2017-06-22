/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.schemabuilder;

import com.datastax.driver.core.DataType;

/**
 * Represents a CQL type containing a user-defined type (UDT) in a SchemaBuilder statement.
 * <p/>
 * Use {@link SchemaBuilder#frozen(String)} or {@link SchemaBuilder#udtLiteral(String)} to build instances of this type.
 */
public final class UDTType implements ColumnType {
    private final String asCQLString;

    private UDTType(String asCQLString) {
        this.asCQLString = asCQLString;
    }

    @Override
    public String asCQLString() {
        return asCQLString;
    }

    static UDTType frozen(String udtName) {
        SchemaStatement.validateNotEmpty(udtName, "UDT name");
        return new UDTType("frozen<" + udtName + ">");
    }

    static UDTType list(UDTType elementType) {
        return new UDTType("list<" + elementType.asCQLString() + ">");
    }

    static UDTType set(UDTType elementType) {
        return new UDTType("set<" + elementType.asCQLString() + ">");
    }

    static UDTType mapWithUDTKey(UDTType keyType, DataType valueType) {
        return new UDTType("map<" + keyType.asCQLString() + ", " + valueType + ">");
    }

    static UDTType mapWithUDTValue(DataType keyType, UDTType valueType) {
        return new UDTType("map<" + keyType + ", " + valueType.asCQLString() + ">");
    }

    static UDTType mapWithUDTKeyAndValue(UDTType keyType, UDTType valueType) {
        return new UDTType("map<" + keyType.asCQLString() + ", " + valueType.asCQLString() + ">");
    }

    static UDTType literal(String literal) {
        SchemaStatement.validateNotEmpty(literal, "UDT type literal");
        return new UDTType(literal);
    }
}
