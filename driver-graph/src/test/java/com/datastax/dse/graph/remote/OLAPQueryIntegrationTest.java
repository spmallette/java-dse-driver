/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph.remote;

import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.CCMGraphTestsOLAPSupport;
import com.datastax.driver.dse.graph.GraphFixtures;
import com.datastax.driver.dse.graph.GraphOptions;
import com.datastax.dse.graph.api.DseGraph;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DseVersion("5.0.0")
public class OLAPQueryIntegrationTest extends CCMGraphTestsOLAPSupport {

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        executeGraph(GraphFixtures.modern);
    }

    private GraphOptions graphOptions(String source) {
        return new GraphOptions()
                .setGraphName(cluster().getConfiguration().getGraphOptions().getGraphName())
                .setGraphSource(source);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void executeQuery(int times, GraphTraversalSource g) {
        for (int i = 0; i < times; i++) {
            // Get vertex clusters of people.  We expect there to be 2 clusters:
            // 1) A cluster with only Peter, he doesn't have a 'knows' relation to anybody.
            // 2) A cluster of vertices around Marko, he knows vadas and josh.
            // The peerPressure step requires a OLAP GraphComputer, thus this should fail without the
            // analytics traversal source.
            Map<Object, Object> clusters = g.V().hasLabel("person")
                    .peerPressure().by("cluster")
                    .group().by("cluster").by("name")
                    .next();

            assertThat(clusters.values()).extracting(o -> Sets.newTreeSet((List) o)).containsOnly(
                    Sets.newTreeSet(Lists.newArrayList("peter")),
                    Sets.newTreeSet(Lists.newArrayList("marko", "josh", "vadas"))
            );
        }
    }

    /**
     * Validates that when performing a traversal that requires an OLAP
     * {@link org.apache.tinkerpop.gremlin.process.computer.GraphComputer} that the traversal is successful when
     * providing a {@link GraphOptions} instance to {@link DseGraph#traversal(DseSession, GraphOptions)} configured
     * with a traversal source of 'a'.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long")
    public void should_successfully_make_query_requiring_olap_with_analytics_source() {
        executeQuery(10, DseGraph.traversal(session(), graphOptions("a")));
    }

    /**
     * Validates that performing a traversal that requires an OLAP
     * {@link org.apache.tinkerpop.gremlin.process.computer.GraphComputer} fails when the {@link GraphOptions} provided
     * to {@link DseGraph#traversal(DseSession, GraphOptions)} is using the 'default' traversal source.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long", expectedExceptions = {InvalidQueryException.class},
            expectedExceptionsMessageRegExp = "VertexComputing steps must be executed with a GraphComputer.*")
    public void should_fail_to_make_query_requiring_olap_using_default_traversal_source() {
        executeQuery(1, DseGraph.traversal(session(), graphOptions("default")));
    }

    /**
     * Validates that performing a traversal that requires an OLAP
     * {@link org.apache.tinkerpop.gremlin.process.computer.GraphComputer} fails when using
     * {@link DseGraph#traversal(DseSession, GraphOptions)} and the underlying {@link com.datastax.driver.dse.DseSession}
     * is not configured with the 'a' traversal source.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long", expectedExceptions = {InvalidQueryException.class},
            expectedExceptionsMessageRegExp = "VertexComputing steps must be executed with a GraphComputer.*")
    public void should_fail_to_make_query_requiring_olap_by_default() {
        executeQuery(1, DseGraph.traversal(session(), session().getCluster().getConfiguration().getGraphOptions()));
    }

}
