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
import com.datastax.dse.graph.api.predicates.Search;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DseVersion(value = "5.0.3")
@CCMConfig(workloads = @CCMWorkload({"solr", "graph"}), jvmArgs = "-Duser.language=en")
public class TextPredicatesIntegrationTest extends CCMTinkerPopTestsSupport {

    TextPredicatesIntegrationTest() {
        super(true);
    }

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        executeGraph(GraphFixtures.textIndices());
        if (ccm().getDSEVersion().compareTo(VersionNumber.parse("5.1.0")) < 0) {
            // In DSE 5.0, it appears that when schema is created it isn't guaranteed
            // that the solr core exists, sleep for 20 seconds to allow it to be created.
            Uninterruptibles.sleepUninterruptibly(20, TimeUnit.SECONDS);
        }
        // reindex to ensure data is indexed.
        ccm().reloadCore(1, graphName(), "user_p", true);
    }

    /**
     * Validates that a graph traversal can be made by using a Search prefix predicate on an indexed property of the
     * given type.
     * <p/>
     * Finds all 'user' vertices having a 'full_name' property beginning with 'Paul'.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long", dataProviderClass = GraphFixtures.class, dataProvider = "indexTypes")
    public void search_by_prefix_search(String indexType) {
        // Only one user with full_name starting with Paul.
        GraphTraversal<Vertex, String> traversal = g.V().has("user", "full_name_" + indexType, Search.prefix("Paul")).values("full_name_" + indexType);
        assertThat(traversal.toList()).containsOnly("Paul Thomas Joe");
    }

    /**
     * Validates that a graph traversal can be made by using a Search regex predicate on an indexed property of the
     * given type.
     * <p/>
     * Finds all 'user' vertices having a 'full_name' property matching regex '.*Paul.*'.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long", dataProviderClass = GraphFixtures.class, dataProvider = "indexTypes")
    public void search_by_regex(String indexType) {
        // Only two people with names containing pattern for Paul.
        GraphTraversal<Vertex, String> traversal = g.V().has("user", "full_name_" + indexType, Search.regex(".*Paul.*")).values("full_name_" + indexType);
        assertThat(traversal.toList()).containsOnly("Paul Thomas Joe", "James Paul Joe");
    }

    /**
     * Validates that a graph traversal can be made by using a Search fuzzy predicate on an indexed property of the
     * given type.
     * <p/>
     * Finds all 'user' vertices having a 'alias' property matching 'awrio' with a fuzzy distance of 1.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long", dataProviderClass = GraphFixtures.class, dataProvider = "indexTypes")
    @DseVersion("5.1.0")
    public void search_by_fuzzy(String indexType) {
        // Alias matches 'awrio' fuzzy
        GraphTraversal<Vertex, String> traversal = g.V().has("user", "alias_" + indexType, Search.fuzzy("awrio", 1)).values("full_name_" + indexType);
        // Should not match 'Paul Thomas Joe' since alias is 'mario', which is at distance 2 of 'awrio' (a -> m, w -> a)
        // Should match 'George Bill Steve' since alias is 'wario' witch matches 'awrio' within a distance of 1 (transpose w with a).
        assertThat(traversal.toList()).containsOnly("George Bill Steve");
    }

    /**
     * Validates that a graph traversal can be made by using a Search token predicate on an indexed property of the
     * given type.
     * <p/>
     * Finds all 'user' vertices having a 'description' property containing the token 'cold'.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long", dataProviderClass = GraphFixtures.class, dataProvider = "indexTypes")
    public void search_by_token(String indexType) {
        // Description containing token 'cold'
        GraphTraversal<Vertex, String> traversal = g.V().has("user", "description_" + indexType, Search.token("cold")).values("full_name_" + indexType);
        assertThat(traversal.toList()).containsOnly("Jill Alice", "George Bill Steve");
    }

    /**
     * Validates that a graph traversal can be made by using a Search token prefix predicate on an indexed property of
     * the given type.
     * <p/>
     * Finds all 'user' vertices having a 'description' containing the token prefix 'h'.
     */
    @Test(groups = "long", dataProviderClass = GraphFixtures.class, dataProvider = "indexTypes")
    public void search_by_token_prefix(String indexType) {
        // Description containing a token starting with h
        GraphTraversal<Vertex, String> traversal = g.V().has("user", "description_" + indexType, Search.tokenPrefix("h")).values("full_name_" + indexType);
        assertThat(traversal.toList()).containsOnly("Paul Thomas Joe", "James Paul Joe");
    }

    /**
     * Validates that a graph traversal can be made by using a Search token regex predicate on an indexed property of
     * the given type.
     * <p/>
     * Finds all 'user' vertices having a 'description' containing the token regex '(nice|hospital)'.
     */
    @Test(groups = "long", dataProviderClass = GraphFixtures.class, dataProvider = "indexTypes")
    public void search_by_token_regex(String indexType) {
        // Description containing nice or hospital
        GraphTraversal<Vertex, String> traversal = g.V().has("user", "description_" + indexType, Search.tokenRegex("(nice|hospital)")).values("full_name_" + indexType);
        assertThat(traversal.toList()).containsOnly("Paul Thomas Joe", "Jill Alice");
    }

    /**
     * Validates that a graph traversal can be made by using a Search fuzzy predicate on an indexed property of
     * the given type.
     * <p/>
     * Finds all 'user' vertices having a 'description' property matching 'lieks' with a fuzzy distance of 1.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long", dataProviderClass = GraphFixtures.class, dataProvider = "indexTypes")
    @DseVersion("5.1.0")
    public void search_by_token_fuzzy(String indexType) {
        // Description containing 'lives' fuzzy
        GraphTraversal<Vertex, String> traversal = g.V().has("user", "description_" + indexType, Search.tokenFuzzy("lieks", 1)).values("full_name_" + indexType);
        // Should not match 'Paul Thomas Joe' since description contains 'Lives' which is at distance of 2 (e -> v, k -> e)
        // Should match 'James Paul Joe' since description contains 'Likes' (transpose e for k)
        assertThat(traversal.toList()).containsOnly("James Paul Joe");
    }

    /**
     * Validates that a graph traversal can be made by using a Search phrase predicate on an indexed property of
     * the given type.
     * <p/>
     * Finds all 'user' vertices having a 'description' property matching 'a cold' with a distance of 2.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long", dataProviderClass = GraphFixtures.class, dataProvider = "indexTypes")
    @DseVersion("5.1.0")
    public void search_by_phrase(String indexType) {
        // Full name contains phrase "Paul Joe"
        GraphTraversal<Vertex, String> traversal = g.V().has("user", "description_" + indexType, Search.phrase("a cold", 2)).values("full_name_" + indexType);
        // Should match 'George Bill Steve' since 'A cold dude' is at distance of 0 for 'a cold'.
        // Should match 'Jill Alice' since 'Enjoys a very nice cold coca cola' is at distance of 2 for 'a cold'.
        assertThat(traversal.toList()).containsOnly("George Bill Steve", "Jill Alice");
    }
}
