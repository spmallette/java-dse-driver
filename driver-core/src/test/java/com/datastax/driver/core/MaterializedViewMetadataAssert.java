/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class MaterializedViewMetadataAssert extends AbstractAssert<MaterializedViewMetadataAssert, MaterializedViewMetadata> {

    public MaterializedViewMetadataAssert(MaterializedViewMetadata actual) {
        super(actual, MaterializedViewMetadataAssert.class);
    }

    public MaterializedViewMetadataAssert hasName(String name) {
        assertThat(actual.getName()).isEqualTo(name);
        return this;
    }

    public MaterializedViewMetadataAssert hasBaseTable(TableMetadata table) {
        assertThat(actual.getBaseTable()).isEqualTo(table);
        return this;
    }

    public MaterializedViewMetadataAssert hasNumberOfColumns(int expected) {
        assertThat(actual.getColumns().size()).isEqualTo(expected);
        return this;
    }

}
