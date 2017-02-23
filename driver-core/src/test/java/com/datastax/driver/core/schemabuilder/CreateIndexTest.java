/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.schemabuilder;

import org.testng.annotations.Test;

import static com.datastax.driver.core.schemabuilder.SchemaBuilder.createIndex;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateIndexTest {

    @Test(groups = "unit")
    public void should_create_index() throws Exception {
        //Given //When
        SchemaStatement statement = createIndex("myIndex").ifNotExists().onTable("ks", "test").andColumn("col");

        //Then
        assertThat(statement.getQueryString()).isEqualTo("\n\tCREATE INDEX IF NOT EXISTS myIndex ON ks.test(col)");
    }

    @Test(groups = "unit")
    public void should_create_index_on_keys_of_map_column() throws Exception {
        //Given //When
        SchemaStatement statement = createIndex("myIndex").ifNotExists().onTable("ks", "test").andKeysOfColumn("col");

        //Then
        assertThat(statement.getQueryString()).isEqualTo("\n\tCREATE INDEX IF NOT EXISTS myIndex ON ks.test(KEYS(col))");
    }

}
