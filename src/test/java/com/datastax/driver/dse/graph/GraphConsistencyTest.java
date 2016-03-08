/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.utils.DseVersion;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

import static com.datastax.driver.core.ConsistencyLevel.ALL;
import static com.datastax.driver.core.ConsistencyLevel.ONE;
import static com.datastax.driver.dse.graph.Assertions.assertThat;

@CCMConfig(numberOfNodes = 3, dirtiesContext = true)
@DseVersion(major = 5.0)
public class GraphConsistencyTest extends CCMGraphTestsSupport {

    @Override
    public CCMBridge.Builder configureCCM() {
        CCMBridge.Builder builder = super.configureCCM().withNodes(3);
        for (int i = 1; i <= 3; i++) {
            builder = builder.withWorkload(i, CCMAccess.Workload.graph)
                    .withDSEConfiguration("graph.gremlin_server.port", "0");
        }
        return builder;
    }

    @Override
    public void onTestContextInitialized() {
        // Create graph with RF 3.
        String graphName = TestUtils.generateIdentifier("graph_");
        String replicationConfig = "{'class': 'SimpleStrategy', 'replication_factor' : 3}";
        session().executeGraph("system.graph(name).option('graph.replication_config')" +
                        ".set(replicationConfig).option('graph.system_replication_config')" +
                        ".set(replicationConfig).ifNotExists().create()",
                ImmutableMap.<String, Object>of("name", graphName, "replicationConfig", replicationConfig));
        cluster().getConfiguration().getGraphOptions().setGraphName(graphName);
        // Set temporarily high read timeout to deal with schema changes made across nodes.
        cluster().getConfiguration().getSocketOptions().setReadTimeoutMillis(32000);
        executeGraph(GraphFixtures.modern);
        // Execute a traversal, which triggers some schema operations, needed before taking a node down.
        session().executeGraph("g.V().limit(1)");
        cluster().getConfiguration().getSocketOptions().setReadTimeoutMillis(12000);
        // Stop node 2.
        ccm().stop(2);
    }

    /**
     * Validates that when a node is down and a graph traversal that only requires reads is made with a
     * CL of {@link ConsistencyLevel#ONE} using {@link GraphStatement#setGraphReadConsistencyLevel(ConsistencyLevel)}
     * that the query is successful.
     *
     * @test_category dse:graph
     * @jira_ticket JAVA-1104
     */
    @Test(groups = "long")
    public void should_be_able_to_make_read_query_with_graph_read_cl_one_and_node_down() {
        GraphResultSet result = session().executeGraph(new SimpleGraphStatement("g.V().limit(1)")
                .setGraphReadConsistencyLevel(ONE)
                .setGraphWriteConsistencyLevel(ALL)
                .setConsistencyLevel(ALL));

        assertThat(result.getAvailableWithoutFetching()).isEqualTo(1);
    }

    /**
     * Validates that when a node is down and a graph traversal that only requires reads is made with a
     * * CL of {@link ConsistencyLevel#ALL} using {@link GraphStatement#setGraphReadConsistencyLevel(ConsistencyLevel)}
     * that the query is unsuccessful because not all replicas are available.
     *
     * @test_category dse:graph
     * @jira_ticket JAVA-1104
     */
    @Test(groups = "long")
    public void should_be_able_to_make_read_query_with_cl_one_and_node_down() {
        GraphResultSet result = session().executeGraph(new SimpleGraphStatement("g.V().limit(1)")
                .setConsistencyLevel(ONE));

        assertThat(result.getAvailableWithoutFetching()).isEqualTo(1);
    }

    /**
     * Validates that when a node is down and a graph traversal that only requires reads is made with a
     * CL of {@link ConsistencyLevel#ONE} using {@link GraphStatement#setConsistencyLevel(ConsistencyLevel)}
     * that the query is successful.
     *
     * @test_category dse:graph
     * @jira_ticket JAVA-1104
     */
    @Test(groups = "long", expectedExceptions = {InvalidQueryException.class})
    public void should_not_be_able_to_make_read_query_with_graph_read_cl_all_and_node_down() {
        session().executeGraph(new SimpleGraphStatement("g.V().limit(1)")
                .setGraphReadConsistencyLevel(ALL)
                .setGraphWriteConsistencyLevel(ONE)
                .setConsistencyLevel(ONE));
    }

    /**
     * Validates that when a node is down and a graph traversal that only requires reads is made with a
     * CL of {@link ConsistencyLevel#ALL} using {@link GraphStatement#setConsistencyLevel(ConsistencyLevel)}
     * that the query is unsuccessful because not all replicas are available.
     *
     * @test_category dse:graph
     * @jira_ticket JAVA-1104
     */
    @Test(groups = "long", expectedExceptions = {InvalidQueryException.class})
    public void should_not_be_able_to_make_read_query_with_cl_all_and_node_down() {
        session().executeGraph(new SimpleGraphStatement("g.V().limit(1)")
                .setConsistencyLevel(ALL));
    }

    /**
     * Validates that when a node is down and a statement is executed that creates a vertex is made with a
     * CL of {@link ConsistencyLevel#ONE} using {@link GraphStatement#setGraphWriteConsistencyLevel(ConsistencyLevel)}
     * that the query is successful.
     *
     * @test_category dse:graph
     * @jira_ticket JAVA-1104
     */
    @Test(groups = "long")
    public void should_be_able_to_make_write_query_with_graph_write_cl_one_and_node_down() {
        GraphResultSet result = session().executeGraph(new SimpleGraphStatement("graph.addVertex(label, 'person', 'name', 'don', 'age', 37)")
                .setGraphWriteConsistencyLevel(ONE)
                .setGraphReadConsistencyLevel(ALL)
                .setConsistencyLevel(ALL));

        assertThat(result.getAvailableWithoutFetching()).isEqualTo(1);
        assertThat(result.one()).asVertex().hasProperty("name", "don");
    }

    /**
     * Validates that when a node is down and a statement is executed that creates a vertex is made with a
     * CL of {@link ConsistencyLevel#ONE} using {@link GraphStatement#setConsistencyLevel(ConsistencyLevel)}
     * that the query is successful.
     *
     * @test_category dse:graph
     * @jira_ticket JAVA-1104
     */
    @Test(groups = "long")
    public void should_be_able_to_make_write_query_with_cl_one_and_node_down() {
        GraphResultSet result = session().executeGraph(new SimpleGraphStatement("graph.addVertex(label, 'person', 'name', 'don2', 'age', 37)")
                .setConsistencyLevel(ONE));

        assertThat(result.getAvailableWithoutFetching()).isEqualTo(1);
        assertThat(result.one()).asVertex().hasProperty("name", "don2");
    }

    /**
     * Validates that when a node is down and a statement is executed that creates a vertex is made with a
     * CL of {@link ConsistencyLevel#ALL} using {@link GraphStatement#setGraphWriteConsistencyLevel(ConsistencyLevel)}
     * that the query is unsuccessful because not all replicas are available.
     *
     * @test_category dse:graph
     * @jira_ticket JAVA-1104
     */
    @Test(groups = "long", expectedExceptions = {InvalidQueryException.class})
    public void should_not_be_able_to_make_write_query_with_graph_write_cl_all_and_node_down() {
        session().executeGraph(new SimpleGraphStatement("graph.addVertex(label, 'person', 'name', 'joe', 'age', 42)")
                .setGraphWriteConsistencyLevel(ALL)
                .setGraphReadConsistencyLevel(ONE)
                .setConsistencyLevel(ONE));
    }

    /**
     * Validates that when a node is down and a statement is executed that creates a vertex is made with a
     * CL of {@link ConsistencyLevel#ALL} using {@link GraphStatement#setConsistencyLevel(ConsistencyLevel)}
     * that the query is unsuccessful because not all replicas are available.
     *
     * @test_category dse:graph
     * @jira_ticket JAVA-1104
     */
    @Test(groups = "long", expectedExceptions = {InvalidQueryException.class})
    public void should_not_be_able_to_make_write_query_with_cl_all_and_node_down() {
        session().executeGraph(new SimpleGraphStatement("graph.addVertex(label, 'person', 'name', 'joe', 'age', 42)")
                .setConsistencyLevel(ALL));
    }
}
