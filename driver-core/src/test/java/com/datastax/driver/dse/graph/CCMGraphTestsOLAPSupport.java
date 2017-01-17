/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.*;
import com.datastax.driver.core.utils.DseVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DseVersion(major = 5.0)
@CCMConfig(dirtiesContext = true)
public class CCMGraphTestsOLAPSupport extends CCMGraphTestsSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphOLAPQueryTest.class);

    // Number of nodes to run with.
    private static final Integer NUM_NODES = 2;

    @Override
    protected void initTestContext(Object testInstance, Method testMethod) throws Exception {
        super.initTestContext(testInstance, testMethod);

        // Wait for master to come online before altering keyspace as it needs to meet LOCAL_QUORUM CL to start, and
        // that can't be met with 1/NUM_NODES available.
        InetSocketAddress masterHttpPort = new InetSocketAddress(TestUtils.ipOfNode(1), 7080);
        LOGGER.debug("Waiting for spark master HTTP interface: {}.", masterHttpPort);
        TestUtils.waitUntilPortIsUp(masterHttpPort);
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
                this.ccm().setWorkload(i, "graph", "spark");
                this.ccm().start(i);
            }

            InetSocketAddress binaryIntf = new InetSocketAddress(TestUtils.ipOfNode(i), this.ccm().getBinaryPort());
            LOGGER.debug("Waiting for binary interface: {}.", binaryIntf);
            TestUtils.waitUntilPortIsUp(binaryIntf);

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
                URL masterHome = new URL(String.format("http://%s:7080", TestUtils.ipOfNode(1)));
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
        // Start with 1 node initially, 2 other nodes will be bootstrapped one at a time.
        return super.configureCCM()
                .withNodes(1)
                .withWorkload(1, "graph", "spark");
    }

    /**
     * Identifies the host that is currently the spark master by checking port 7077 being open on each host in metadata
     * and returning the first Host that is listening on that port.
     *
     * @return The spark master found in cluster metadata.
     */
    public Host findSparkMaster() {
        for (Host host : cluster().getMetadata().getAllHosts()) {
            if (TestUtils.pingPort(host.getAddress(), 7077)) {
                return host;
            }
        }
        return null;
    }
}
