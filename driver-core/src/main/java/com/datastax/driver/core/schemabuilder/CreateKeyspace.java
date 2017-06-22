/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.schemabuilder;

/**
 * A built CREATE KEYSPACE statement.
 */
public class CreateKeyspace {

    static final String command = "CREATE KEYSPACE";

    private final String keyspaceName;
    private boolean ifNotExists;

    public CreateKeyspace(String keyspaceName) {
        this.keyspaceName = keyspaceName;
        this.ifNotExists = false;
    }

    public CreateKeyspace ifNotExists() {
        this.ifNotExists = true;
        return this;
    }

    /**
     * Add options for this CREATE KEYSPACE statement.
     *
     * @return the options of this CREATE KEYSPACE statement.
     */
    public KeyspaceOptions with() {
        return new KeyspaceOptions(buildCommand(), keyspaceName);
    }

    String buildCommand() {
        StringBuilder createStatement = new StringBuilder();
        createStatement.append(command);
        if (ifNotExists) {
            createStatement.append(" IF NOT EXISTS");
        }
        return createStatement.toString();
    }

}
