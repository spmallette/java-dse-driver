/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.CCMBridge;
import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.DseCluster;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DseVersion("5.0.0")
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
    @Test(groups = "short")
    public void should_be_able_to_make_graph_query() {
        int count = session().executeGraph("g.V().count()").one().asInt();
        assertThat(count).isEqualTo(6);
    }

    @Override
    public DseCluster.Builder createClusterBuilder() {
        return super.createClusterBuilder().withCredentials("cassandra", "cassandra");
    }
}
