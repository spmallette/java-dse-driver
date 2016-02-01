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
package com.datastax.driver.dse.graph;

import org.testng.annotations.Test;

import java.util.List;

import static com.datastax.driver.dse.graph.Assertions.assertThat;

/**
 * This test requires non-released DSE functionality.
 * <p/>
 * To run this test, define the system property {@code cassandra.directory} and point it
 * to a valid DSE installation. This directory should contain
 * a local DSE git repository. It should be previously compiled and built.
 * <p/>
 * Note that using {@code cassandra.branch} does NOT work with DSE.
 */
public class GraphTest extends CCMGraphTestsSupport {

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        executeGraph(
                "g.addV(label, 'comic', 'name', \"DD\", 'issues-number', 381)",
                "g.addV(label, 'character', 'name', \"BLACK PANTHER/T'CHAL\")",
                "g.V().hasLabel('character').has('name', \"BLACK PANTHER/T'CHAL\").next()" +
                        ".addEdge('appears_in', " +
                        "g.V().hasLabel('comic').has('name', \"DD\").next(), 'issue', \"DD 52\")");
    }

    // TODO Move these out to other classes where applicable.
    // TODO replace these if adequate functionality is provided in other classes.

    @Test(groups = "short")
    public void should_retrieve_all_vertices_using_g_V() throws Exception {
        GraphResultSet rs = session().executeGraph(new SimpleGraphStatement("g.V()"));
        assertThat(rs.getAvailableWithoutFetching()).isEqualTo(2);
        for (GraphResult result : rs) {
            Vertex v = result.asVertex();
            if (v.getLabel().equals("character")) {
                assertThat(v).hasLabel("character").hasProperty("name", "BLACK PANTHER/T'CHAL");
            } else {
                assertThat(v).hasLabel("comic").hasProperty("issues-number", 381);
            }
        }
    }

    @Test(groups = "short")
    public void should_retrieve_all_edges_using_g_E() throws Exception {
        GraphResultSet rs = session().executeGraph(new SimpleGraphStatement("g.E()"));
        // There should only be one edge.
        assertThat(rs.getAvailableWithoutFetching()).isEqualTo(1);
        assertThat(rs.one())
                .asEdge()
                .hasLabel("appears_in")
                .hasProperty("issue", "DD 52")
                .hasInVLabel("comic")
                .hasOutVLabel("character");
    }

    @Test(groups = "short")
    public void should_retrieve_all_vertex_labels() throws Exception {
        GraphResultSet rs = session().executeGraph(new SimpleGraphStatement("g.V().label()"));
        List<GraphResult> results = rs.all();
        assertThat(results).extractingResultOf("asString").containsOnly("character", "comic");
    }

    @Test(groups = "short")
    public void should_retrieve_character_by_name() throws Exception {
        SimpleGraphStatement stmt = new SimpleGraphStatement("g.V().hasLabel('character').has('name', name)");
        stmt.set("name", "BLACK PANTHER/T'CHAL");
        GraphResultSet rs = session().executeGraph(stmt);
        for (GraphResult result : rs) {
            assertThat(result)
                    .asVertex()
                    .hasLabel("character")
                    .hasProperty("name", "BLACK PANTHER/T'CHAL");
        }
    }

    @Test(groups = "short")
    public void should_retrieve_comic_by_issue() throws Exception {
        SimpleGraphStatement stmt = new SimpleGraphStatement("g.V().hasLabel('comic').has('issues-number', number)");
        stmt.set("number", 381);
        GraphResultSet rs = session().executeGraph(stmt);
        for (GraphResult result : rs) {
            assertThat(result)
                    .asVertex()
                    .hasLabel("comic")
                    .hasProperty("issues-number", 381);
        }
    }

    @Test(groups = "short")
    public void should_be_able_retrieve_path_of_comics_a_character_appears_in() throws Exception {
        String query = "g.V().hasLabel('character').has('name', \"BLACK PANTHER/T'CHAL\").outE().inV().path()";
        GraphResultSet rs = session().executeGraph(query);
        Path path = rs.one().asPath();
        assertThat(path.getObjects()).hasSize(3);
        assertThat(path)
                .object(0)
                .asVertex()
                .hasLabel("character")
                .hasProperty("name", "BLACK PANTHER/T'CHAL");
        assertThat(path)
                .object(1)
                .asEdge()
                .hasLabel("appears_in")
                .hasProperty("issue", "DD 52")
                .hasInVLabel("comic")
                .hasInV(path.getObjects().get(2))
                .hasOutVLabel("character")
                .hasOutV(path.getObjects().get(0));
        assertThat(path)
                .object(2)
                .asVertex()
                .hasLabel("comic")
                .hasProperty("issues-number", 381);
    }
}
