/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph.remote;

import com.datastax.driver.core.CCMConfig;
import com.datastax.driver.core.CCMWorkload;
import com.datastax.driver.core.VersionNumber;
import com.datastax.driver.core.exceptions.InvalidQueryException;
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
import static org.assertj.core.api.Assertions.fail;

@DseVersion(value = "5.1.0")
@CCMConfig(workloads = @CCMWorkload({"solr", "graph"}), jvmArgs = "-Duser.language=en")
@SuppressWarnings("unchecked")
public class GeoPredicatesIndexingIntegrationTest extends CCMTinkerPopTestsSupport {

    GeoPredicatesIndexingIntegrationTest() {
        super(true);
    }

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        executeGraph(GraphFixtures.geoIndices(ccm().getDSEVersion()));
        // reindex to ensure data is indexed.
        ccm().reloadCore(1, graphName(), "user_p", true);
    }

    /**
     * Validates the Geometry distance algorithm when using a non-indexed Point property.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long", dataProviderClass = GraphFixtures.class,  dataProvider = "indexTypes")
    public void search_by_distance_cartesian(String indexType) {
        // in cartesian geometry, the distance between POINT(30 30) and POINT(40 40) is exactly 14.142135623730951
        // any point further than that should be detected outside of the range.
        // the vertex "Paul Thomas Joe" is at POINT(40.0001 40), and shouldn't be detected inside the range.
        GraphTraversal<Vertex, String> traversal = g.V().has("user", "pointPropWithBounds_" + indexType, Geo.inside(Geo.point(30, 30), 14.142135623730951)).values("full_name");
        assertThat(traversal.toList()).containsOnly("George Bill Steve", "Jill Alice");
    }

    /**
     * Validates the Geometry distance algorithm when using a non-indexed Point property.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long", dataProviderClass = GraphFixtures.class,  dataProvider = "indexTypes")
    public void search_by_distance_geodetic(String indexType) {
        // in geodetic geometry, the distance between POINT(30 30) and POINT(40 40) is exactly 12.908258700131379
        // any point further than that should be detected outside of the range.
        // the vertex "Paul Thomas Joe" is at POINT(40.0001 40), and shouldn't be detected inside the range.
        GraphTraversal<Vertex, String> traversal = g.V().has("user", "pointPropWithGeoBounds_" + indexType, Geo.inside(Geo.point(30, 30), 12.908258700131379, Geo.Unit.DEGREES)).values("full_name");
        assertThat(traversal.toList()).containsOnly("George Bill Steve", "Jill Alice");
    }

    /**
     * Validates that the geodetic predicates cannot be used against a point defined in cartesian geometry.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long",
            expectedExceptions = InvalidQueryException.class, expectedExceptionsMessageRegExp = "Distance units cannot be used in queries against non-geodetic points.")
    public void should_fail_if_geodetic_predicate_used_against_cartesian_property_with_search_index() {
        GraphTraversal<Vertex, String> traversal = g.V().has("user", "pointPropWithBounds_search", Geo.inside(Geo.point(30, 30), 12.908258700131379, Geo.Unit.DEGREES)).values("full_name");
        traversal.toList();
        fail("Should have failed executing the traversal because the property type is incorrect");
    }

    /**
     * Validates that the cartesian predicates cannot be used against a point defined in geodetic geometry.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long",
            expectedExceptions = InvalidQueryException.class, expectedExceptionsMessageRegExp = "Distance units are required for queries against geodetic points.")
    public void should_fail_if_cartesian_predicate_used_against_geodetic_property_with_search_index() {
        GraphTraversal<Vertex, String> traversal = g.V().has("user", "pointPropWithGeoBounds_search", Geo.inside(Geo.point(30, 30), 14.142135623730951)).values("full_name");
        traversal.toList();
        fail("Should have failed executing the traversal because the property type is incorrect");
    }
}
