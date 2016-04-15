/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.CCMBridge;
import com.datastax.driver.core.Cluster;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphAuthenticationTest extends CCMGraphTestsSupport {
    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        executeGraph(GraphFixtures.modern);
    }

    @Override
    public CCMBridge.Builder configureCCM() {
        return super.configureCCM()
                .withCassandraConfiguration("authenticator", "PasswordAuthenticator")
                .withJvmArgs("-Dcassandra.superuser_setup_delay_ms=0");
    }

    /**
     * Ensure that graph queries can be made over an authenticated connection.
     *
     * @test_category dse:graph
     */
    @Test(groups="short")
    public void should_be_able_to_make_graph_query() {
        int count = session().executeGraph("g.V().count()").one().asInt();
        assertThat(count).isEqualTo(6);
    }

    @Override
    public Cluster.Builder createClusterBuilder() {
        return super.createClusterBuilder().withCredentials("cassandra", "cassandra");
    }
}
