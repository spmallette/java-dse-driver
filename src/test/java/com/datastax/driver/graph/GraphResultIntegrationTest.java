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

import com.datastax.driver.core.utils.DseVersion;
import com.google.common.collect.Lists;
import org.assertj.core.api.iterable.Extractor;
import org.testng.annotations.Test;

import java.util.List;

import static com.datastax.driver.graph.Assertions.assertThat;

@DseVersion(major = 5.0)
public class GraphResultIntegrationTest extends CCMGraphTestsSupport {

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        executeGraph(GraphFixtures.modern);
    }

    /**
     * Ensures that a traversal that returns an object with labels can be properly represented as graph result.
     * <p/>
     * Executes a vertex traversal that binds label 'a' and 'b' to vertex properties and label 'c' to vertices that
     * have edges from that vertex.
     */
    @Test(groups = "short")
    public void object_of_mixed_types() {
        // find all software vertices and select name, language, and find all vertices that created such software.
        GraphResultSet rs = session().executeGraph("g.V().hasLabel('software').as('a', 'b', 'c')." +
                "select('a','b','c')." +
                "by('name')." +
                "by('lang')." +
                "by(__.in('created').fold())");


        assertThat(rs.getAvailableWithoutFetching()).isEqualTo(2);
        List<GraphResult> results = rs.all();

        // Ensure that we got 'lop' and 'ripple' for property a.
        assertThat(results).extracting(new Extractor<GraphResult, String>() {
            @Override
            public String extract(GraphResult input) {
                return input.get("a").asString();
            }
        }).containsOnly("lop", "ripple");

        for (GraphResult result : results) {
            // both software are written in java.
            assertThat(result.get("b").asString()).isEqualTo("java");
            // TODO: Ensure that property 'c' contains the expected vertices,
            // need some mechanism of addressing it as a collection, for now we'll just grab each index.
            GraphResult c = result.get("c");
            if (result.get("a").asString().equals("lop")) {
                // 'c' should contain marko, josh, peter.
                // Ensure we have three vertices.
                assertThat(c).hasChild(2);
                List<Vertex> vertices = Lists.newArrayList(c.get(0).asVertex(), c.get(1).asVertex(), c.get(2).asVertex());
                assertThat(vertices).extracting(new Extractor<Vertex, String>() {
                    @Override
                    public String extract(Vertex input) {
                        return input.getProperties().get("name").asString();
                    }
                }).containsOnly("marko", "josh", "peter");
            } else {
                // ripple, 'c' should contain josh.
                // Ensure we have 1 vertex.
                assertThat(c).hasChild(0);
                Vertex vertex = c.get(0).asVertex();
                assertThat(vertex).hasProperty("name", "josh");
            }
        }
    }

    // TODO: positive and negative tests for each as* method of GraphResult.
    // TODO: null tests.
}
