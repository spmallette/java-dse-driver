/*
 *      Copyright (C) 2012-2015 DataStax Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.datastax.driver.graph;

import com.datastax.driver.DseCluster;
import com.datastax.driver.DseSession;
import com.datastax.driver.core.CCMConfig;
import com.datastax.driver.core.CCMTestsSupport;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.TestUtils;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;

import static com.datastax.driver.core.CCMAccess.Workload.graph;

@CCMConfig(createKeyspace = false, dse = true, workloads = graph)
public class CCMGraphTestsSupport extends CCMTestsSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(CCMGraphTestsSupport.class);

    private final String graphName = TestUtils.generateIdentifier("graph_");

    @Override
    public Cluster.Builder createClusterBuilder() {
        return DseCluster.builder().withQueryOptions(TestUtils.nonDebouncingQueryOptions());
    }

    @Override
    public void onTestContextInitialized() {
        // TODO only create the graph schema if told to via annotation config
        // Create the graph schema and set the namespace in the graph configuration to it.
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
}
