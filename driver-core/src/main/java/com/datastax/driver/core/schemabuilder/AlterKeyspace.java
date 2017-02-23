/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.schemabuilder;

/**
 * A built ALTER KEYSPACE statement.
 */
public class AlterKeyspace {

    static final String COMMAND = "ALTER KEYSPACE";

    private final String keyspaceName;

    public AlterKeyspace(String keyspaceName) {
        this.keyspaceName = keyspaceName;
    }

    /**
     * Add options for this ALTER KEYSPACE statement.
     *
     * @return the options of this ALTER KEYSPACE statement.
     */
    public KeyspaceOptions with() {
        return new KeyspaceOptions(COMMAND, keyspaceName);
    }

}

