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

import com.datastax.driver.core.utils.DseVersion;
import org.testng.annotations.Test;

import java.util.List;

import static com.datastax.driver.dse.graph.Assertions.assertThat;

@DseVersion(major = 5.0)
public class GraphOptionsIntegrationTest extends CCMGraphTestsSupport {

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        executeGraph(GraphFixtures.modern);
    }

    /**
     * Ensures that if a non-existing graph name is used that an error is thrown and the driver handles it gracefully.
     * <p/>
     * As DSE currently returns a SERVER_ERROR there is currently not a good way to handle this, so this test is
     * disabled.
     *
     * @test_category dse:graph
     */
    @Test(groups = "short", enabled = false)
    public void should_handle_error_when_graph_name_doesnt_exist() {
        GraphOptions options = cluster().getConfiguration().getGraphOptions();
        String originalName = options.getGraphName();
        try {
            options.setGraphName("nonExisting");
            session().executeGraph("g.V()");
        } finally {
            options.setGraphName(originalName);
        }
    }

    /**
     * Validates that if the graph alias is set on GraphOptions that the aliased name can be used to represent the
     * graph in a gremlin query.
     *
     * @test_category dse:graph
     */
    @Test(groups = "short", enabled = true)
    public void should_use_alias_if_configured_in_graph_options() {
        GraphOptions options = cluster().getConfiguration().getGraphOptions();
        String originalAlias = options.getGraphAlias();
        try {
            options.setGraphAlias("myalias");
            List<GraphResult> result = session().executeGraph("myalias.V().hasLabel('software')").all();
            assertThat(result).hasSize(2);
        } finally {
            options.setGraphAlias(originalAlias);
        }
    }

    /**
     * Validates that if the graph alias is set on GraphStatement that the aliased name can be used to represent the
     * graph in a gremlin query.
     *
     * @test_category dse:graph
     */
    @Test(groups = "short", enabled = true)
    public void should_use_alias_if_configured_in_statement() {
        GraphStatement stmt = new SimpleGraphStatement("h.V().hasLabel('software')").setGraphAlias("h");
        List<GraphResult> result = session().executeGraph(stmt).all();
        assertThat(result).hasSize(2);
    }

    // TODO tests for graph source and language.
}
