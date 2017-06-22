/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph.remote;

import com.datastax.driver.core.CCMConfig;
import com.datastax.driver.core.CCMWorkload;
import com.datastax.driver.core.VersionNumber;
import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.graph.GraphFixtures;
import com.datastax.dse.graph.CCMTinkerPopTestsSupport;
import com.datastax.dse.graph.api.predicates.Geo;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DseVersion(value = "5.0.3", description = "DSE 5.0.3 required for remote TinkerPop support")
@CCMConfig(workloads = @CCMWorkload({"solr", "graph"}), jvmArgs = "-Duser.language=en")
@SuppressWarnings("unchecked")
public class GeoPredicatesIntegrationTest extends CCMTinkerPopTestsSupport {

    GeoPredicatesIntegrationTest(){
        super(true);
    }

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        executeGraph(GraphFixtures.addressBook(ccm().getDSEVersion()));
        if (ccm().getDSEVersion().compareTo(VersionNumber.parse("5.1.0")) < 0) {
            // In DSE 5.0, it appears that when schema is created it isn't guaranteed
            // that the solr core exists, sleep for 20 seconds to allow it to be created.
            Uninterruptibles.sleepUninterruptibly(20, TimeUnit.SECONDS);
        }
        // reindex to ensure data is indexed.
        ccm().reloadCore(1, graphName(), "user_p", true);
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


    /**
     * Validates that a graph traversal can be made by using an 'inside' distance predicate on a string-search-indexed
     * property with 'degrees' units.
     * <p/>
     * Finds all 'user' vertices having a 'coordinates' property 2 degrees within -94, 44.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long")
    public void search_by_distance_degrees() {
        // Should only be two people within 2 units of (-92, 44) (Rochester, Minneapolis)
        GraphTraversal<Vertex, String> traversal = g.V().has("user", "coordinates", Geo.inside(Geo.point(-92, 44), 2, Geo.Unit.DEGREES)).values("full_name");
        assertThat(traversal.toList()).containsOnly("Paul Thomas Joe", "George Bill Steve");
    }

    /**
     * Validates that a graph traversal can be made by using an 'inside' distance predicate on a string-search-indexed
     * property with 'miles' units.
     * <p/>
     * Finds all 'user' vertices having a 'coordinates' property 190 nautical miles within -89.39, 43.06.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long")
    public void search_by_distance_miles() {
        // Should only be two people within 190 miles of Madison, WI (-89.39, 43.06) (Rochester, Chicago)
        // Minneapolis is too far away (~200 miles).
        GraphTraversal<Vertex, String> traversal = g.V().has("user", "coordinates", Geo.inside(Geo.point(-89.39, 43.06), 190, Geo.Unit.MILES)).values("full_name");
        assertThat(traversal.toList()).containsOnly("Paul Thomas Joe", "James Paul Joe");
    }

    /**
     * Validates that a graph traversal can be made by using an 'inside' distance predicate on a string-search-indexed
     * property with 'kilometers' units.
     * <p/>
     * Finds all 'user' vertices having a 'coordinates' property 400 kilometers within -93.60, 41.60
     *
     * @test_category dse:graph
     */
    @Test(groups = "long")
    public void search_by_distance_kilometers() {
        // Should only be two people within 400 KM of Des Moines, IA (-93.60, 41.60) (Rochester, Minneapolis)
        // Chicago is too far away (~500 KM)
        GraphTraversal<Vertex, String> traversal = g.V().has("user", "coordinates", Geo.inside(Geo.point(-93.60, 41.60), 400, Geo.Unit.KILOMETERS)).values("full_name");
        assertThat(traversal.toList()).containsOnly("Paul Thomas Joe", "George Bill Steve");
    }

    /**
     * Validates that a graph traversal can be made by using an 'inside' distance predicate on a string-search-indexed
     * property with 'meters' units.
     * <p/>
     * Finds all 'user' vertices having a 'coordinates' property 350000 meters within -93.60, 41.60
     *
     * @test_category dse:graph
     */
    @Test(groups = "long")
    public void search_by_distance_meters() {
        // Should only be on person within 350,000 M of Des Moines, IA (-93.60, 41.60) (Rochester)
        GraphTraversal<Vertex, String> traversal = g.V().has("user", "coordinates", Geo.inside(Geo.point(-93.60, 41.60), 350000, Geo.Unit.METERS)).values("full_name");
        assertThat(traversal.toList()).containsOnly("Paul Thomas Joe");
    }

    /**
     * Validates that a graph traversal can be made by using an 'inside' polygon predicate.
     * <p/>
     * Finds all 'user' vertices having a 'coordinates' inside a polygon that only Chicago and Rochester fit in.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long")
    @DseVersion(value = "5.1.0", description = "Requires 5.1.0 for insideCartesian predicate")
    public void search_by_polygon_area() {
        // 10 clicks from La Crosse, WI should include Chicago, Rochester and Minneapolis, this is needed to filter
        // down the traversal set using the search index as Geo.inside(polygon) is not supported for search indices.
        // Filter further by an area that only Chicago and Rochester fit in. (Minneapolis is too far west.
        GraphTraversal<Vertex, String> traversal = g.V().has("user", "coordinates", Geo.inside(Geo.point(-91.2, 43.8), 10, Geo.Unit.DEGREES))
                .local(__.has("coordinates", Geo.inside(Geo.polygon(-82, 40, -92.5, 45, -95, 38, -82, 40))))
                .values("full_name");
        assertThat(traversal.toList()).containsOnly("Paul Thomas Joe", "James Paul Joe");
    }
}
