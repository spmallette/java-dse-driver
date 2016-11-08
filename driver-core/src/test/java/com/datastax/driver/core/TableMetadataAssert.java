/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class TableMetadataAssert extends AbstractAssert<TableMetadataAssert, TableMetadata> {
    protected TableMetadataAssert(TableMetadata actual) {
        super(actual, TableMetadataAssert.class);
    }

    public TableMetadataAssert hasName(String name) {
        assertThat(actual.getName()).isEqualTo(name);
        return this;
    }

    public TableMetadataAssert isInKeyspace(String keyspaceName) {
        assertThat(actual.getKeyspace().getName()).isEqualTo(keyspaceName);
        return this;
    }

    public TableMetadataAssert hasColumn(String columnName) {
        assertThat(actual.getColumn(columnName)).isNotNull();
        return this;
    }

    public TableMetadataAssert hasColumn(String columnName, DataType dataType) {
        ColumnMetadata column = actual.getColumn(columnName);
        assertThat(column).isNotNull();
        assertThat(column.getType()).isEqualTo(dataType);
        return this;
    }

    public TableMetadataAssert hasNoColumn(String columnName) {
        assertThat(actual.getColumn(columnName)).isNull();
        return this;
    }

    public TableMetadataAssert hasComment(String comment) {
        assertThat(actual.getOptions().getComment()).isEqualTo(comment);
        return this;
    }

    public TableMetadataAssert doesNotHaveComment(String comment) {
        assertThat(actual.getOptions().getComment()).isNotEqualTo(comment);
        return this;
    }

    public TableMetadataAssert isCompactStorage() {
        assertThat(actual.getOptions().isCompactStorage()).isTrue();
        return this;
    }

    public TableMetadataAssert isNotCompactStorage() {
        assertThat(actual.getOptions().isCompactStorage()).isFalse();
        return this;
    }

    public TableMetadataAssert hasNumberOfColumns(int expected) {
        assertThat(actual.getColumns().size()).isEqualTo(expected);
        return this;
    }

    public TableMetadataAssert hasMaterializedView(MaterializedViewMetadata expected) {
        assertThat(actual.getView(Metadata.quote(expected.getName()))).isNotNull().isEqualTo(expected);
        return this;
    }

    public TableMetadataAssert hasIndex(IndexMetadata index) {
        assertThat(actual.getIndexes()).contains(index);
        return this;
    }
}
