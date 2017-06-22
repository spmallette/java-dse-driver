/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.datastax.driver.core.exceptions.OperationTimedOutException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.scassandra.http.client.PrimingRequest.queryBuilder;
import static org.scassandra.http.client.PrimingRequest.then;

public class ReadTimeoutTest extends ScassandraTestBase.PerClassCluster {

    String query = "SELECT foo FROM bar";

    @BeforeMethod(groups = "short")
    public void setup() {
        primingClient.prime(
                queryBuilder()
                        .withQuery(query)
                        .withThen(then().withFixedDelay(100L))
                        .build()
        );

        // Set default timeout too low
        cluster.getConfiguration().getSocketOptions().setReadTimeoutMillis(10);
    }

    @Test(groups = "short", expectedExceptions = OperationTimedOutException.class)
    public void should_use_default_timeout_if_not_overridden_by_statement() {
        session.execute(query);
    }

    @Test(groups = "short")
    public void should_use_statement_timeout_if_overridden() {
        Statement statement = new SimpleStatement(query).setReadTimeoutMillis(10000);
        session.execute(statement);
    }

    @Test(groups = "short")
    public void should_disable_timeout_if_set_to_zero_at_statement_level() {
        Statement statement = new SimpleStatement(query).setReadTimeoutMillis(0);
        session.execute(statement);
    }
}
