/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.ServerError;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.scassandra.http.client.PrimingRequest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.datastax.driver.core.TestUtils.nonQuietClusterCloseOptions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.scassandra.http.client.PrimingRequest.Result.server_error;
import static org.scassandra.http.client.PrimingRequest.then;

public class LoggingIdempotenceAwareRetryPolicyTest {

    Logger logger = Logger.getLogger(LoggingIdempotenceAwareRetryPolicy.class);

    ScassandraCluster scassandras;

    DseCluster cluster;

    MemoryAppender logs;

    Level originalLevel;

    static final String query = "mock query";

    @BeforeMethod(groups = "short")
    public void beforeMethod() {
        scassandras = ScassandraCluster.builder().withNodes(2).build();
        scassandras.init();

        cluster = DseCluster.builder()
                .addContactPoints(scassandras.address(1).getAddress())
                .withPort(scassandras.getBinaryPort())
                .withLoadBalancingPolicy(new SortingLoadBalancingPolicy())
                // Scassandra does not support V3 nor V4 yet, and V4 may cause the server to crash
                .withProtocolVersion(ProtocolVersion.V2)
                // Set many core connections so errors can happen multiple times on a host.
                .withPoolingOptions(new PoolingOptions()
                        .setCoreConnectionsPerHost(HostDistance.LOCAL, 8)
                        .setMaxConnectionsPerHost(HostDistance.LOCAL, 8)
                        .setHeartbeatIntervalSeconds(0))
                .withNettyOptions(nonQuietClusterCloseOptions)
                .build();

        // Prime node 1 to respond with server error for query.
        PrimingRequest prime = PrimingRequest.queryBuilder()
                .withQuery(query)
                .withThen(then().withResult(server_error))
                .build();
        scassandras.node(1).primingClient().prime(prime);

        originalLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        logs = new MemoryAppender();
        logger.addAppender(logs);
    }

    @AfterMethod(groups = "short", alwaysRun = true)
    public void afterMethod() {
        logger.setLevel(originalLevel);
        logger.removeAppender(logs);
        if (cluster != null)
            cluster.close();
        if (scassandras != null)
            scassandras.stop();
    }

    /**
     * Validates that a warning is emitted on cluster init that an idempotence aware retry policy is in use.
     *
     * @jira_ticket JAVA-1225
     * @test_category retry_policy
     */
    @Test(groups="short")
    public void should_log_warning_on_cluster_init() {
        // clear any previous logs.
        logs.getNext();
        cluster.init();
        assertThat(logs.getNext()).contains("Initializing cluster with idempotence-aware retry policy.");
    }

    /**
     * Validates that a warning is emitted when a non-idempotent query failed and will not be retried, but only
     * on the first occurrence.
     *
     * @jira_ticket JAVA-1225
     * @test_category retry_policy
     */
    @Test(groups="short")
    public void should_log_warning_on_non_retry_of_non_idempotent_query_at_most_once() {
        DseSession session = cluster.connect();
        logs.getNext();
        String expectedLogMessage = "Not retrying statement because it is not idempotent";
        int serverErrorCount = 0;

        try {
            for(int i = 0; i < 10; i++) {
                try {
                    Statement stmt = new SimpleStatement(query).setIdempotent(false);
                    ResultSet result = session.execute(stmt);
                    Host triedHost = result.getExecutionInfo().getTriedHosts().get(0);
                    // every other query should hit host 2 which will succeed, ensure host 1 failed.
                    if(triedHost.getAddress().toString().split(":")[0].endsWith("1")) {
                        fail("Expected ServerError");
                    }
                } catch (ServerError exception) {
                    serverErrorCount++;
                    String log = logs.getNext();
                    if(serverErrorCount == 1) {
                        assertThat(log).contains(expectedLogMessage);
                    } else {
                        assertThat(log).doesNotContain(expectedLogMessage);
                    }
                }
            }
        } finally {
            session.close();
        }
        assertThat(serverErrorCount).isGreaterThan(1);
    }

    /**
     * Validates that a warning is not emitted when an idempotent query failed and that it is retried successfully.
     *
     * @jira_ticket JAVA-1225
     * @test_category retry_policy
     */
    @Test(groups="short")
    public void should_retry_idempotent_statement_on_server_error() {
        DseSession session = cluster.connect();
        logs.getNext();

        try {
            Statement stmt = new SimpleStatement(query).setIdempotent(true);
            ResultSet result = session.execute(stmt);
            assertThat(result.getExecutionInfo().getTriedHosts()).hasSize(2);
            String log = logs.getNext();

            assertThat(log).doesNotContain("Not retrying statement because it is not idempotent");
        } finally {
            session.close();
        }
    }
}
