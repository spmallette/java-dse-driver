/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

import com.datastax.driver.core.CCMConfig;
import com.datastax.driver.core.CCMTestsSupport;
import com.datastax.driver.core.utils.CassandraVersion;
import org.testng.annotations.Test;

@CassandraVersion("2.2.0")
@CCMConfig(config = "enable_user_defined_functions:true")
public class FunctionExecutionExceptionTest extends CCMTestsSupport {


    @Override
    public void onTestContextInitialized() {
        execute(
                "CREATE TABLE foo (k int primary key, i int, l list<int>)",
                "INSERT INTO foo (k, i, l) VALUES (1, 1, [1])",
                "CREATE FUNCTION element_at(l list<int>, i int) RETURNS NULL ON NULL INPUT RETURNS int LANGUAGE java AS 'return (Integer) l.get(i);'"
        );
    }

    @Test(groups = "short", expectedExceptions = FunctionExecutionException.class)
    public void should_throw_when_function_execution_fails() {
        session().execute("SELECT element_at(l, i) FROM foo WHERE k = 1");
    }
}
