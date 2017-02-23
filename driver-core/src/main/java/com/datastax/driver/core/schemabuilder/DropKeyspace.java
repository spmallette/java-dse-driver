/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.schemabuilder;

/**
 * A built DROP KEYSPACE statement.
 */
public class DropKeyspace extends SchemaStatement {

    private final String keyspaceName;
    private boolean ifExists;

    public DropKeyspace(String keyspaceName) {
        this.keyspaceName = keyspaceName;
        this.ifExists = false;
        validateNotEmpty(keyspaceName, "Keyspace name");
        validateNotKeyWord(keyspaceName,
                String.format("The keyspace name '%s' is not allowed because it is a reserved keyword", keyspaceName));
    }

    /**
     * Add the 'IF EXISTS' condition to this DROP statement.
     *
     * @return this statement.
     */
    public DropKeyspace ifExists() {
        this.ifExists = true;
        return this;
    }

    @Override
    public String buildInternal() {
        StringBuilder dropStatement = new StringBuilder("DROP KEYSPACE ");
        if (ifExists) {
            dropStatement.append("IF EXISTS ");
        }
        dropStatement.append(keyspaceName);
        return dropStatement.toString();
    }

}
