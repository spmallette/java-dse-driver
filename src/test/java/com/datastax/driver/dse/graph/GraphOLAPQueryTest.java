/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.*;
import com.datastax.driver.core.utils.DseVersion;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DseVersion(major = 5.0)
@CCMConfig(dirtiesContext = true)
public class GraphOLAPQueryTest extends CCMGraphTestsSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphOLAPQueryTest.class);

    // Number of nodes to run with.
    private static final Integer NUM_NODES = 2;

    @Override
    protected void initTestContext(Object testInstance, Method testMethod) throws Exception {
        super.initTestContext(testInstance, testMethod);
        // Set the dse_leases keyspace to RF of NUM_NODES, this will prevent election of new job tracker until all nodes
        // are available, preventing weird cases where 1 node thinks the wrong node is a master.
        Cluster tempCluster = createClusterBuilder().addContactPointsWithPorts(getContactPointsWithPorts()).build();
        try {
            Session session = tempCluster.connect();
            session.execute("ALTER KEYSPACE dse_leases WITH REPLICATION = {'class': 'NetworkTopologyStrategy', 'GraphAnalytics': '" + NUM_NODES + "'}");
        } finally {
            tempCluster.close();
        }
        // Bootstrap additional nodes, waiting for binary interface and for it to come up as a spark worker.
        for (int i = 1; i <= NUM_NODES; i++) {
            if (i != 1) {
                this.ccm().add(i);
                this.ccm().setWorkload(i, CCMAccess.Workload.graph, CCMAccess.Workload.spark);
                this.ccm().start(i);
            }

            InetSocketAddress binaryIntf = new InetSocketAddress(TestUtils.ipOfNode(i), this.ccm().getBinaryPort());
            LOGGER.debug("Waiting for binary interface: {}.", binaryIntf);
            TestUtils.waitUntilPortIsUp(binaryIntf);

            InetSocketAddress masterHttpPort = new InetSocketAddress("localhost", 7080);
            LOGGER.debug("Waiting for spark master HTTP interface: {}.", masterHttpPort);
            TestUtils.waitUntilPortIsUp(masterHttpPort);

            waitForWorkers(i);
        }
    }

    /**
     * Wait for workerCount spark workers to come online.
     *
     * @param workerCount Number of workers to expect up.
     */
    private void waitForWorkers(final int workerCount) {
        LOGGER.debug("Waiting for {} workers to be alive.", workerCount);
        ConditionChecker.check().that(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                URL masterHome = new URL("http://localhost:7080");
                HttpURLConnection connection = (HttpURLConnection) masterHome.openConnection();
                connection.setRequestMethod("GET");
                BufferedReader rd = null;
                try {
                    rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    Pattern aliveWorkersPattern = Pattern.compile("Alive Workers:.*(\\d+)</li>");
                    while ((line = rd.readLine()) != null) {
                        Matcher matcher = aliveWorkersPattern.matcher(line);
                        if (matcher.find()) {
                            Integer numWorkers = Integer.parseInt(matcher.group(1));
                            if (numWorkers != workerCount) {
                                LOGGER.debug("Only {}/{} workers are alive.", numWorkers, workerCount);
                                return false;
                            } else {
                                LOGGER.debug("{}/{} workers now alive.", workerCount, workerCount);
                                return true;
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.debug("Error encountered while checking master URL for worker count.", e);
                } finally {
                    if (rd != null) {
                        rd.close();
                    }
                }
                LOGGER.debug("Could not find alive workers text.");
                return false;
            }
        }).every(1, TimeUnit.SECONDS).before(5, TimeUnit.MINUTES).becomesTrue();
    }

    @Override
    public void onTestContextInitialized() {
        createAndSetGraphConfig(NUM_NODES);
        executeGraph(GraphFixtures.modern);
    }

    @Override
    public CCMBridge.Builder configureCCM() {
        // Unfortunately binary port 9042 is explicitly required for internode communication (without more
        // dse specific configuration code).
        // Start with 1 node initially, 2 other nodes will be bootstrapped one at a time.
        CCMBridge.Builder builder = super.configureCCM()
                .withBinaryPort(9042)
                .withNodes(1)
                .withWorkload(1, CCMAccess.Workload.graph, CCMAccess.Workload.spark);
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
