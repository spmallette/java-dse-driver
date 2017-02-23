/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.querybuilder;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.TableMetadata;

import java.util.Arrays;
import java.util.List;

/**
 * A built TRUNCATE statement.
 */
public class Truncate extends BuiltStatement {

    private final String table;

    Truncate(String keyspace, String table) {
        this(keyspace, table, null, null);
    }

    Truncate(TableMetadata table) {
        this(escapeId(table.getKeyspace().getName()),
                escapeId(table.getName()),
                Arrays.asList(new Object[table.getPartitionKey().size()]),
                table.getPartitionKey());
    }

    Truncate(String keyspace,
             String table,
             List<Object> routingKeyValues,
             List<ColumnMetadata> partitionKey) {
        super(keyspace, partitionKey, routingKeyValues);
        this.table = table;
    }

    @Override
    protected StringBuilder buildQueryString(List<Object> variables, CodecRegistry codecRegistry) {
        StringBuilder builder = new StringBuilder();

        builder.append("TRUNCATE ");
        if (keyspace != null)
            Utils.appendName(keyspace, builder).append('.');
        Utils.appendName(table, builder);

        return builder;
    }
}
