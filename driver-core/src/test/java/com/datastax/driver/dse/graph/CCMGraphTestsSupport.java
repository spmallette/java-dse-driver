/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.CCMBridge;
import com.datastax.driver.core.CCMConfig;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.TestUtils;
import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.CCMDseTestsSupport;
import com.datastax.driver.dse.DseCluster;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Uninterruptibles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static com.datastax.driver.core.CCMBridge.Builder.RANDOM_PORT;

@CCMConfig(createKeyspace = false, dse = true, ccmProvider = "configureCCM")
@DseVersion("5.0.0")
public class CCMGraphTestsSupport extends CCMDseTestsSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(CCMGraphTestsSupport.class);

    private final String graphName = TestUtils.generateIdentifier("graph_");

    @Override
    public DseCluster.Builder createClusterBuilder() {
        return super.createClusterBuilder()
                // avoid timeouts when creating large graphs
                .withSocketOptions(new SocketOptions().setReadTimeoutMillis(60000));
    }

    @Override
    public void onTestContextInitialized() {
        // TODO only create the graph schema if told to via annotation config
        // Create the graph schema and set the namespace in the graph configuration to it.
        createAndSetGraphConfig(1);
    }

    /**
     * Creates a graph and sets the cluster() configuration to use that graph's name.
     *
     * @param rf Replication factor for the graph's data and system keyspaces.
     */
    public void createAndSetGraphConfig(int rf) {
        String replicationConfig = "{'class': 'SimpleStrategy', 'replication_factor' : " + rf + "}";
        session().executeGraph("system.graph(name).option('graph.replication_config')" +
                        ".set(replicationConfig).option('graph.system_replication_config')" +
                        ".set(replicationConfig).ifNotExists().create()",
                ImmutableMap.<String, Object>of("name", graphName, "replicationConfig", replicationConfig));
        cluster().getConfiguration().getGraphOptions().setGraphName(graphName);
    }

    /**
     * Executes the given graph statements with the test's session object.
     * <p/>
     * Useful to create test fixtures and/or load data before tests.
     * <p/>
     * This method should not be called if a session object hasn't been created
     * (if CCM configuration specifies {@code createSession = false})
     *
     * @param statements The statements to execute.
     */
    public void executeGraph(String... statements) {
        executeGraph(Arrays.asList(statements));
    }

    /**
     * Executes the given graph statements with the test's session object.
     * <p/>
     * Useful to create test fixtures and/or load data before tests.
     * <p/>
     * This method should not be called if a session object hasn't been created
     * (if CCM configuration specifies {@code createSession = false})
     *
     * @param statements The statements to execute.
     */
    public void executeGraph(Collection<String> statements) {
        assert session() != null;
        for (String stmt : statements) {
            try {
                // Unfortunately we need to sleep between schema queries with multi-node clusters until
                // DSP-9376 is fixed.
                if (cluster().getMetadata().getAllHosts().size() > 1) {
                    Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
                }
                session().executeGraph(stmt);
            } catch (Exception e) {
                errorOut();
                LOGGER.error("Could not execute graph statement: " + stmt, e);
                Throwables.propagate(e);
            }
        }
    }

    public CCMBridge.Builder configureCCM() {
        return CCMBridge.builder().withWorkload(1, "graph")
                .withDSEConfiguration("graph.gremlin_server.port", RANDOM_PORT);
    }

    public String graphName() {
        return graphName;
    }
}
