/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

/**
 * Identifies a PreparedStatement.
 */
public class PreparedId {
    // This class is mostly here to group PreparedStatement data that are need for
    // execution but that we don't want to expose publicly (see JAVA-195)
    final MD5Digest id;

    final ColumnDefinitions metadata;
    final ColumnDefinitions resultSetMetadata;

    final int[] routingKeyIndexes;
    final ProtocolVersion protocolVersion;

    PreparedId(MD5Digest id, ColumnDefinitions metadata, ColumnDefinitions resultSetMetadata, int[] routingKeyIndexes, ProtocolVersion protocolVersion) {
        this.id = id;
        this.metadata = metadata;
        this.resultSetMetadata = resultSetMetadata;
        this.routingKeyIndexes = routingKeyIndexes;
        this.protocolVersion = protocolVersion;
    }
}
