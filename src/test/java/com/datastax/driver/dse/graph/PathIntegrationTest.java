/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.utils.DseVersion;
import org.testng.annotations.Test;

import static com.datastax.driver.dse.graph.GraphAssertions.assertThat;

@DseVersion(major = 5.0)
public class PathIntegrationTest extends CCMGraphTestsSupport {

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        executeGraph(GraphFixtures.modern);
    }

    /**
     * Validates that when traversing a path and labeling some of the elements during the traversal that the
     * output elements are properly labeled.
     *
     * @test_category dse:graph
     */
    @Test(groups = "short")
    public void should_resolve_path_with_some_labels() {
        GraphResultSet rs = session().executeGraph("g.V().hasLabel('person').has('name', 'marko').as('a')" +
                ".outE('knows').inV().as('c', 'd').outE('created').as('e', 'f', 'g').inV().path()");
        assertThat(rs.getAvailableWithoutFetching()).isEqualTo(2);
        for (GraphNode result : rs) {
            Path path = result.asPath();
            validatePathObjects(path);
            assertThat(path.getLabels()).hasSize(5);
            assertThat(path)
                    .hasLabel(0, "a")
                    .hasNoLabel(1)
                    .hasLabel(2, "c", "d")
                    .hasLabel(3, "e", "f", "g")
                    .hasNoLabel(4);
        }
    }

    /**
     * Validates that when traversing a path and labeling all of the elements during the traversal that the
     * output elements are properly labeled.
     *
     * @test_category dse:graph
     */
    @Test(groups = "short")
    public void should_resolve_path_with_labels() {
        GraphResultSet rs = session().executeGraph("g.V().hasLabel('person').has('name', 'marko').as('a')" +
                ".outE('knows').as('b').inV().as('c', 'd').outE('created').as('e', 'f', 'g').inV().as('h').path()");
        assertThat(rs.getAvailableWithoutFetching()).isEqualTo(2);
        for (GraphNode result : rs) {
            Path path = result.asPath();
            validatePathObjects(path);
            assertThat(path.getLabels()).hasSize(5);
            assertThat(path)
                    .hasLabel(0, "a")
                    .hasLabel(1, "b")
                    .hasLabel(2, "c", "d")
                    .hasLabel(3, "e", "f", "g")
                    .hasLabel(4, "h");
        }
    }

    /**
     * Validates that when traversing a path and labeling none of the elements during the traversal that all the
     * labels are empty in the result.
     *
     * @test_category dse:graph
     */
    @Test(groups = "short")
    public void should_resolve_path_without_labels() {
        GraphResultSet rs = session().executeGraph("g.V().hasLabel('person').has('name', 'marko')" +
                ".outE('knows').inV().outE('created').inV().path()");
        assertThat(rs.getAvailableWithoutFetching()).isEqualTo(2);
        for (GraphNode result : rs) {
            Path path = result.asPath();
            validatePathObjects(path);
            assertThat(path.getLabels()).hasSize(5);
            for (int i = 0; i < 5; i++)
                assertThat(path)
                        .hasNoLabel(i);
        }
    }

    /**
     * Ensures that the given Path matches one of the exact traversals we'd expect for a person whom Marko
     * knows that has created software and what software that is.
     * <p/>
     * These paths should be:
     * <ul>
     * <li>marko -> knows -> josh -> created -> lop</li>
     * <li>marko -> knows -> josh -> created -> ripple</li>
     * </ul>
     */
    private void validatePathObjects(Path path) {

        // marko should be the origin point.
        assertThat(path)
                .object(0)
                .asVertex()
                .hasLabel("person")
                .hasProperty("name", "marko")
                .hasProperty("age", 29);

        // there should be a 'knows' outgoing relationship between marko and josh.
        assertThat(path)
                .object(1)
                .asEdge()
                .hasLabel("knows")
                .hasProperty("weight", 1)
                .hasOutVLabel("person")
                .hasOutV(path.getObjects().get(0))
                .hasInVLabel("person")
                .hasInV(path.getObjects().get(2));

        // josh...
        assertThat(path)
                .object(2)
                .asVertex()
                .hasLabel("person")
                .hasProperty("name", "josh")
                .hasProperty("age", 32);

        if (path.getObjects().get(4).asVertex().getProperty("name").getValue().asString().equals("lop")) {

            // there should be a 'created' relationship between josh and lop.
            assertThat(path)
                    .object(3)
                    .asEdge()
                    .hasLabel("created")
                    .hasProperty("weight", 0.4)
                    .hasOutVLabel("person")
                    .hasOutV(path.getObjects().get(2))
                    .hasInVLabel("software")
                    .hasInV(path.getObjects().get(4));

            // lop..
            assertThat(path)
                    .object(4)
                    .asVertex()
                    .hasLabel("software")
                    .hasProperty("name", "lop")
                    .hasProperty("lang", "java");

        } else {

            // there should be a 'created' relationship between josh and ripple.
            assertThat(path)
                    .object(3)
                    .asEdge()
                    .hasLabel("created")
                    .hasProperty("weight", 1.0)
                    .hasOutVLabel("person")
                    .hasOutV(path.getObjects().get(2))
                    .hasInVLabel("software")
                    .hasInV(path.getObjects().get(4));

            // ripple..
            assertThat(path)
                    .object(4)
                    .asVertex()
                    .hasLabel("software")
                    .hasProperty("name", "ripple")
                    .hasProperty("lang", "java");
        }
    }

}
