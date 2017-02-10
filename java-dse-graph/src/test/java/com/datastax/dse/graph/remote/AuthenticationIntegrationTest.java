/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph.remote;

import com.datastax.driver.core.CCMBridge;
import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.DseCluster;
import com.datastax.dse.graph.CCMTinkerPopTestsSupport;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DseVersion(value = "5.0.3", description = "DSE 5.0.3 required for remote TinkerPop support")
public class AuthenticationIntegrationTest extends CCMTinkerPopTestsSupport {

    AuthenticationIntegrationTest() {
        super(true);
    }

    @Override
    public CCMBridge.Builder configureCCM() {
        return super.configureCCM()
                .withCassandraConfiguration("authenticator", "PasswordAuthenticator")
                .withJvmArgs("-Dcassandra.superuser_setup_delay_ms=0");
    }

    /**
     * Ensure that graph traversals can be made over an authenticated connection.
     *
     * @test_category dse:graph
     */
    @Test(groups = "short")
    public void should_be_able_to_make_graph_query() {
        assertThat(g.V().count().next()).isEqualTo(6L);
    }

    @Override
    public DseCluster.Builder createClusterBuilder() {
        return super.createClusterBuilder().withCredentials("cassandra", "cassandra");
    }
}
