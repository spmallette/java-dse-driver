/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.CCMBridge;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

import static com.datastax.driver.dse.graph.Assertions.assertThat;

public class AuthenticationTest extends CCMGraphTestsSupport {

    @SuppressWarnings("unused")
    public CCMBridge.Builder configureCCM() {
        return super.configureCCM()
                .withCassandraConfiguration("authenticator", "PasswordAuthenticator")
                .withJvmArgs("-Dcassandra.superuser_setup_delay_ms=0");
    }

    @Override
    public Cluster.Builder createClusterBuilder() {
        return super.createClusterBuilder()
                .withAuthProvider(new PlainTextAuthProvider("cassandra", "cassandra"));
    }

    /**
     * Validates that queries can be executed over an authenticated interface.
     *
     * @test_category dse:graph
     */
    @Test(groups = "short", enabled = false)
    public void should_be_able_to_create_vertex() {
        // TODO: Reenable when DSP-8191 fixed.
        GraphResult result = session().executeGraph("g.addV(label, 'person', 'name', name)",
                ImmutableMap.<String, Object>of("name", "andy")).one();
        assertThat(result).asVertex().hasLabel("person").hasProperty("name", "andy");
    }
}
