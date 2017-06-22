/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.testng.annotations.Test;

import static com.datastax.driver.core.Assertions.assertThat;

public class PreparedIdTest extends CCMTestsSupport {

    @Override
    public void onTestContextInitialized() {
        execute(
                "CREATE TABLE foo(k1 int, k2 int, k3 int, v int, PRIMARY KEY ((k1, k2, k3)))"
        );
    }

    /**
     * Validates that the correct routing key indexes are present for a fully-bound prepared statement.
     *
     * @test_category prepared_statements:metadata
     */
    @Test(groups = "short")
    public void should_have_routing_key_indexes_when_all_bound() {
        PreparedStatement pst = session().prepare("INSERT INTO foo (k3, k1, k2, v) VALUES (?, ?, ?, ?)");
        assertThat(pst.getPreparedId().routingKeyIndexes).containsExactly(1, 2, 0);
    }

    /**
     * Validates that no routing key indexes are present for a partially-bound prepared statement.
     *
     * @test_category prepared_statements:metadata
     */
    @Test(groups = "short")
    public void should_not_have_routing_key_indexes_when_some_not_bound() {
        PreparedStatement pst = session().prepare("INSERT INTO foo (k3, k1, k2, v) VALUES (1, ?, ?, ?)");
        assertThat(pst.getPreparedId().routingKeyIndexes).isNull();
    }

    /**
     * Validates that no routing key indexes are present for a none-bound prepared statement.
     *
     * @test_category prepared_statements:metadata
     */
    @Test(groups = "short")
    public void should_not_have_routing_key_indexes_when_none_bound() {
        PreparedStatement pst = session().prepare("INSERT INTO foo (k3, k1, k2, v) VALUES (1, 1, 1, 1)");
        assertThat(pst.getPreparedId().routingKeyIndexes).isNull();
    }
}
