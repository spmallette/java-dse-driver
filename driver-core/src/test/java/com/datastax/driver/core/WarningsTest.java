/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.datastax.driver.core.utils.CassandraVersion;
import com.google.common.base.Strings;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CCMConfig(config = {"batch_size_warn_threshold_in_kb:5"})
@CassandraVersion("2.2.0")
public class WarningsTest extends CCMTestsSupport {

    @Override
    public void onTestContextInitialized() {
        execute("CREATE TABLE foo(k int primary key, v text)");
    }

    @Test(groups = "short")
    public void should_expose_warnings_on_execution_info() {
        // the default batch size warn threshold is 5 * 1024 bytes, but after CASSANDRA-10876 there must be
        // multiple mutations in a batch to trigger this warning so the batch includes 2 different inserts.
        ResultSet rs = session().execute(String.format("BEGIN UNLOGGED BATCH\n" +
                        "INSERT INTO foo (k, v) VALUES (1, '%s')\n" +
                        "INSERT INTO foo (k, v) VALUES (2, '%s')\n" +
                        "APPLY BATCH",
                Strings.repeat("1", 2 * 1024),
                Strings.repeat("1", 3 * 1024)));

        List<String> warnings = rs.getExecutionInfo().getWarnings();
        assertThat(warnings).hasSize(1);
    }
}
