/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.schemabuilder;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.datastax.driver.core.schemabuilder.SchemaBuilder.alterKeyspace;
import static org.assertj.core.api.Assertions.assertThat;

public class AlterKeyspaceTest {

    @Test(groups = "unit")
    public void should_alter_keyspace_with_options() throws Exception {
        Map<String, Object> replicationOptions = new HashMap<String, Object>();
        replicationOptions.put("class", "SimpleStrategy");
        replicationOptions.put("replication_factor", 1);

        //When
        SchemaStatement statement = alterKeyspace("test").with()
                .durableWrites(true)
                .replication(replicationOptions);

        //Then
        assertThat(statement.getQueryString())
                .isEqualTo("\n\tALTER KEYSPACE test" +
                           "\n\tWITH\n\t\t" +
                           "REPLICATION = {'replication_factor': 1, 'class': 'SimpleStrategy'}\n\t\t" +
                           "AND DURABLE_WRITES = true");
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void incorrect_replication_options() throws Exception {
        Map<String, Object> replicationOptions = new HashMap<String, Object>();
        replicationOptions.put("class", 5);

        //When
        alterKeyspace("test").with()
                .replication(replicationOptions);
    }
}
