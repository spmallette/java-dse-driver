/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph.remote;

import com.datastax.driver.core.CCMConfig;
import com.datastax.driver.core.CCMWorkload;
import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.graph.GraphFixtures;
import com.datastax.dse.graph.CCMTinkerPopTestsSupport;
import com.datastax.dse.graph.api.predicates.Geo;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DseVersion(value = "5.0.3", description = "DSE 5.0.3 required for remote TinkerPop support")
@CCMConfig(workloads = @CCMWorkload({"solr", "graph"}), jvmArgs = "-Duser.language=en")
@SuppressWarnings("unchecked")
public class GeoIntegrationTest extends CCMTinkerPopTestsSupport {

    GeoIntegrationTest(){
        super(true);
    }

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        executeGraph(GraphFixtures.addressBook(ccm().getDSEVersion()));
        // arbitrary sleep to deal with index time.
        // TODO: Find a better way of dealing with this uncertainty.
        Uninterruptibles.sleepUninterruptibly(20, TimeUnit.SECONDS);
    }

    /**
     * Validates correctness of {@link Geo#point} with the fluent API.
     * <p/>
     * Finds the only user that has the point (-92.46295 44.0234) as defined in {@link com.datastax.driver.dse.graph.GraphFixtures}.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long")
    public void should_find_geo_point_with_fluent_api() {
        GraphTraversal<Vertex, String> traversal = g.V().hasLabel("user").has("coordinates", Geo.point(-92.46295, 44.0234)).values("full_name");
        assertThat(traversal.toList()).containsOnly("Paul Thomas Joe");
    }

    /**
     * Validates correctness of {@link Geo#lineString} with the fluent API.
     * <p/>
     * Finds the only user that has the lineString (30 10, 10 30, 40 40) as defined in {@link com.datastax.driver.dse.graph.GraphFixtures}.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long")
    public void should_find_geo_linestring_with_fluent_api() {
        GraphTraversal<Vertex, String> traversal = g.V().hasLabel("user").has("linestringProp", Geo.lineString(30, 10, 10, 30, 40, 40)).values("full_name");
        assertThat(traversal.toList()).containsOnly("George Bill Steve");
    }

    /**
     * Validates correctness of {@link Geo#polygon} with the fluent API.
     * <p/>
     * Finds the only user that has the polygon (30 10, 40 40, 20 40, 10 20, 30 10) as defined in {@link com.datastax.driver.dse.graph.GraphFixtures}.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long")
    public void should_find_geo_polygon_with_fluent_api() {
        GraphTraversal<Vertex, String> traversal = g.V().hasLabel("user").has("polygonProp", Geo.polygon(30, 10, 40, 40, 20, 40, 10, 20, 30, 10)).values("full_name");
        assertThat(traversal.toList()).containsOnly("James Paul Joe");
    }

}
