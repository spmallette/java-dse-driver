/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.testng.annotations.Test;

public class BatchStatementTest {

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void should_fail_if_adding_statement_with_proxy_authentication() {
        BatchStatement batch = new BatchStatement();
        batch.add(new SimpleStatement("test").executingAs("admin"));
    }
}
