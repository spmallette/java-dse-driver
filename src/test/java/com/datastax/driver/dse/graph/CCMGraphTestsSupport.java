/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.CCMBridge;
import com.datastax.driver.core.CCMConfig;
import com.datastax.driver.core.TestUtils;
import com.datastax.driver.dse.CCMDseTestsSupport;
import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;

import static com.datastax.driver.core.CCMAccess.Workload.graph;

@CCMConfig(createKeyspace = false, dse = true)
public class CCMGraphTestsSupport extends CCMDseTestsSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(CCMGraphTestsSupport.class);

    @Override
    public void onTestContextInitialized() {
        // TODO only create the graph schema if told to via annotation config
        // Create the graph schema and set the namespace in the graph configuration to it.
        String graphName = TestUtils.generateIdentifier("graph_");
        session().executeGraph("system.createGraph(name).ifNotExist().build()",
                ImmutableMap.<String, Object>of("name", graphName));
        cluster().getConfiguration().getGraphOptions().setGraphName(graphName);
    }

    @Override
    public DseSession session() {
        return (DseSession) super.session();
    }

    @Override
    public DseCluster cluster() {
        return (DseCluster) super.cluster();
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
                session().executeGraph(stmt);
            } catch (Exception e) {
                errorOut();
                LOGGER.error("Could not execute graph statement: " + stmt, e);
                Throwables.propagate(e);
            }
        }
    }

    @Override
    public CCMBridge.Builder configureCCM() {
        return super.configureCCM().withWorkload(1, graph);
    }
}
