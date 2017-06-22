/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.datastax.driver.core.exceptions.UnsupportedFeatureException;
import org.testng.SkipException;
import org.testng.annotations.Test;

import static com.datastax.driver.core.ProtocolVersion.V1;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests targeting protocol v1 specifically.
 */
@CCMConfig(version = "1.2.19", dse = false)
public class ProtocolV1Test extends CCMTestsSupport {

    @Override
    public Cluster.Builder createClusterBuilder() {
        return super.createClusterBuilder()
                .withProtocolVersion(V1);
    }

    @Override
    public void beforeTestClass(Object testInstance) throws Exception {
        if (CCMBridge.isWindows())
            throw new SkipException("C* 1.2 is not supported on Windows.");
        super.beforeTestClass(testInstance);
    }

    /**
     * Validates that a simple query with no variables is correctly executed.
     * @jira_ticket JAVA-1132
     */
    @Test(groups = "short")
    public void should_execute_query_with_no_variables() throws Exception {
        session().execute("select * from system.local");
    }

    /**
     * Validates that a simple query with variables is not allowed with protocol V1.
     * (Values in protocol V1 are only allowed in prepared statements).
     * @jira_ticket JAVA-1132
     */
    @Test(groups = "short")
    public void should_not_execute_query_with_variables() throws Exception {
        try {
            session().execute(new SimpleStatement("select * from system.local where key=?", "local"));
        } catch (UnsupportedFeatureException e) {
            assertThat(e).hasMessageContaining("Unsupported feature with the native protocol V1 (which is currently in use): Binary values are not supported");
        }
    }

    /**
     * Validates that a prepared statement with no variables is correctly prepared and executed.
     * @jira_ticket JAVA-1132
     */
    @Test(groups = "short")
    public void should_execute_prepared_statement_with_no_variables() throws Exception {
        PreparedStatement ps = session().prepare("select * from system.local");
        session().execute(ps.bind());
    }

    /**
     * Validates that a prepared statement with variables is correctly prepared and executed.
     * @jira_ticket JAVA-1132
     */
    @Test(groups = "short")
    public void should_execute_prepared_statement_with_variables() throws Exception {
        PreparedStatement ps = session().prepare("select * from system.local where key=?");
        session().execute(ps.bind("local"));
    }

}
