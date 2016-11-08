/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.schemabuilder;

import com.google.common.base.Optional;

/**
 * A built DROP statement.
 */
public class Drop extends SchemaStatement {

    enum DroppedItem {TABLE, TYPE, INDEX}

    private Optional<String> keyspaceName = Optional.absent();
    private String itemName;
    private boolean ifExists;
    private final String itemType;

    Drop(String keyspaceName, String itemName, DroppedItem itemType) {
        this.itemType = itemType.name();
        validateNotEmpty(keyspaceName, "Keyspace name");
        validateNotEmpty(itemName, this.itemType.toLowerCase() + " name");
        validateNotKeyWord(keyspaceName, String.format("The keyspace name '%s' is not allowed because it is a reserved keyword", keyspaceName));
        validateNotKeyWord(itemName, String.format("The " + this.itemType.toLowerCase() + " name '%s' is not allowed because it is a reserved keyword", itemName));
        this.itemName = itemName;
        this.keyspaceName = Optional.fromNullable(keyspaceName);
    }

    Drop(String itemName, DroppedItem itemType) {
        this.itemType = itemType.name();
        validateNotEmpty(itemName, this.itemType.toLowerCase() + " name");
        validateNotKeyWord(itemName, String.format("The " + this.itemType.toLowerCase() + " name '%s' is not allowed because it is a reserved keyword", itemName));
        this.itemName = itemName;
    }

    /**
     * Add the 'IF EXISTS' condition to this DROP statement.
     *
     * @return this statement.
     */
    public Drop ifExists() {
        this.ifExists = true;
        return this;
    }

    @Override
    public String buildInternal() {
        StringBuilder dropStatement = new StringBuilder("DROP " + itemType + " ");
        if (ifExists) {
            dropStatement.append("IF EXISTS ");
        }
        if (keyspaceName.isPresent()) {
            dropStatement.append(keyspaceName.get()).append(".");
        }

        dropStatement.append(itemName);
        return dropStatement.toString();
    }

    /**
     * Generate a DROP TABLE statement
     *
     * @return the final DROP TABLE statement
     */
    public String build() {
        return this.buildInternal();
    }
}
