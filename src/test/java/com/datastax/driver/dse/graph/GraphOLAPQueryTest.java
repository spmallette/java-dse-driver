/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.*;
import com.datastax.driver.core.utils.DseVersion;
import com.google.common.collect.Lists;
import org.testng.annotations.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@DseVersion(major = 5.0)
public class GraphOLAPQueryTest extends CCMGraphTestsSupport {

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        executeGraph(GraphFixtures.modern);
    }

    @Override
    public CCMBridge.Builder configureCCM() {
        // Unfortunately binary port 9042 is explicitly required for internode communication (without more
        // dse specific configuration code).
        CCMBridge.Builder builder = super.configureCCM().withNodes(3)
                .withBinaryPort(9042);
        // Configure each node with graph and spark workload.
        for (int i = 1; i <= 3; i++) {
            builder = builder
                    .withWorkload(i, CCMAccess.Workload.graph, CCMAccess.Workload.spark);
        }
        return builder;
    }

    /**
     * Identifies the host that is currently the spark master by checking port 7077 being open on each host in metadata
     * and returning the first Host that is listening on that port.
     *
     * @return The spark master found in cluster metadata.
     */
    private Host findSparkMaster() {
        for (Host host : cluster().getMetadata().getAllHosts()) {
            if (TestUtils.pingPort(host.getAddress(), 7077)) {
                return host;
            }
        }
        return null;
    }

    private Collection<Host> executeOLAPQuery(int times, String graphSource) {
        // Set a rather large timeout to account for spark queries having initially high overhead.
        cluster().getConfiguration().getSocketOptions().setReadTimeoutMillis(120000);
        GraphStatement statement = new SimpleGraphStatement("g.V().count()");
        if (graphSource != null) {
            statement = statement.setGraphSource(graphSource);
        }
        Collection<Host> triedHosts = Lists.newArrayListWithCapacity(times);
        for (int i = 0; i < times; i++) {
            GraphResultSet result = session().executeGraph(statement);
            assertThat(result.getAvailableWithoutFetching()).isEqualTo(1);
            GraphResult r = result.one();
            assertThat(r.asInt()).isEqualTo(6);

            ExecutionInfo executionInfo = result.getExecutionInfo();
            assertThat(executionInfo.getTriedHosts().size()).isGreaterThanOrEqualTo(1);
            triedHosts.add(executionInfo.getTriedHosts().get(0));
        }
        return triedHosts;
    }

    /**
     * Validates that when using the default load balancing policy that if you make a query with 'a' traversal source
     * that the {@link com.datastax.driver.dse.HostTargetingLoadBalancingPolicy} behavior kicks in and targets
     * the spark master as the primary query source.
     *
     * @test_category dse:graph
     * @jira_ticket JAVA-1098
     */
    @Test(groups = "short")
    public void should_target_analytics_node_with_analytics_source() {
        Host analyticsHost = findSparkMaster();
        assertThat(executeOLAPQuery(10, "a")).containsOnly(analyticsHost);
    }

    /**
     * Validates that when using the default load balancing policy that if you make a query with the default traversal
     * source that {@link com.datastax.driver.dse.HostTargetingLoadBalancingPolicy}
     * does not target the spark master as the primary query source.
     *
     * @test_category dse:graph
     * @jira_ticket JAVA-1098
     */
    @Test(groups = "short")
    public void should_not_target_analytics_node_with_default_source() {
        assertThat(executeOLAPQuery(10, "default")).containsAll(cluster().getMetadata().getAllHosts());
    }

    /**
     * Validates that when using the default load balancing policy that if you make a query without providing a
     * traversal source that {@link com.datastax.driver.dse.HostTargetingLoadBalancingPolicy}
     * does not target the spark master as the primary query source.
     *
     * @test_category dse:graph
     * @jira_ticket JAVA-1098
     */
    @Test(groups = "short")
    public void should_not_target_analytics_node_by_default() {
        assertThat(executeOLAPQuery(10, null)).containsAll(cluster().getMetadata().getAllHosts());
    }
}
