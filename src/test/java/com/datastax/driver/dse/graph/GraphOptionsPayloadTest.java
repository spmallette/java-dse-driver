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

import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphOptionsPayloadTest {

    @Test(groups = "unit")
    public void should_use_default_options_when_none_set() {
        GraphOptions graphOptions = new GraphOptions();

        Map<String, ByteBuffer> expectedPayload = buildPayloadFromProperties(null, GraphOptions.DEFAULT_GRAPH_LANGUAGE, null, GraphOptions.DEFAULT_GRAPH_SOURCE);

        Map<String, ByteBuffer> resultPayload = graphOptions.buildPayloadWithDefaults(new SimpleGraphStatement(""));

        assertThat(resultPayload).isEqualTo(expectedPayload);
    }

    @Test(groups = "unit")
    public void should_use_cluster_options_set() {
        GraphOptions graphOptions = new GraphOptions();
        graphOptions.setGraphAlias("alias1");
        graphOptions.setGraphLanguage("language1");
        graphOptions.setGraphName("name1");
        graphOptions.setGraphSource("source1");

        Map<String, ByteBuffer> expectedPayload = buildPayloadFromProperties("alias1", "language1", "name1", "source1");

        Map<String, ByteBuffer> resultPayload = graphOptions.buildPayloadWithDefaults(new SimpleGraphStatement(""));

        assertThat(resultPayload).isEqualTo(expectedPayload);
    }

    @Test(groups = "unit")
    public void should_use_statement_options_over_cluster_options() {

        GraphOptions graphOptions = new GraphOptions();
        graphOptions.setGraphAlias("alias1");
        graphOptions.setGraphLanguage("language1");
        graphOptions.setGraphName("name1");
        graphOptions.setGraphSource("source1");

        SimpleGraphStatement simpleGraphStatement = new SimpleGraphStatement("");
        simpleGraphStatement.setGraphAlias("alias2");
        simpleGraphStatement.setGraphLanguage("language2");
        simpleGraphStatement.setGraphName("name2");
        simpleGraphStatement.setGraphSource("source2");

        Map<String, ByteBuffer> expectedPayload = buildPayloadFromStatement(simpleGraphStatement);

        Map<String, ByteBuffer> resultPayload = graphOptions.buildPayloadWithDefaults(simpleGraphStatement);

        assertThat(resultPayload).isEqualTo(expectedPayload);
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "graphLanguage cannot be null")
    public void should_not_allow_null_on_graph_language_on_cluster() {
        GraphOptions graphOptions = new GraphOptions();

        graphOptions.setGraphLanguage(null);
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "graphSource cannot be null")
    public void should_not_allow_null_on_graph_source_on_cluster() {
        GraphOptions graphOptions = new GraphOptions();

        graphOptions.setGraphSource(null);
    }

    @Test(groups = "unit")
    public void should_allow_null_graph_name_and_graph_alias_on_cluster() {
        GraphOptions graphOptions = new GraphOptions();
        graphOptions.setGraphLanguage("language1");
        graphOptions.setGraphSource("source1");
        graphOptions.setGraphName(null);
        graphOptions.setGraphAlias(null);

        Map<String, ByteBuffer> expectedPayload = buildPayloadFromProperties(null, "language1", null, "source1");

        Map<String, ByteBuffer> resultPayload = graphOptions.buildPayloadWithDefaults(new SimpleGraphStatement(""));

        assertThat(resultPayload).isEqualTo(expectedPayload);

    }

    @Test(groups = "unit")
    public void should_force_no_graph_name_if_statement_is_a_system_query() {
        GraphOptions graphOptions = new GraphOptions();
        graphOptions.setGraphAlias("alias1");
        graphOptions.setGraphLanguage("language1");
        graphOptions.setGraphName("name1");
        graphOptions.setGraphSource("source1");

        SimpleGraphStatement simpleGraphStatement = new SimpleGraphStatement("");
        simpleGraphStatement.setSystemQuery();

        Map<String, ByteBuffer> expectedPayload = buildPayloadFromProperties("alias1", "language1", null, "source1");

        Map<String, ByteBuffer> resultPayload = graphOptions.buildPayloadWithDefaults(simpleGraphStatement);

        assertThat(resultPayload).isEqualTo(expectedPayload);
    }

    private Map<String, ByteBuffer> buildPayloadFromStatement(GraphStatement graphStatement) {
        return ImmutableMap.of(
                GraphOptions.GRAPH_ALIAS_KEY, PayloadHelper.asBytes(graphStatement.getGraphAlias()),
                GraphOptions.GRAPH_LANGUAGE_KEY, PayloadHelper.asBytes(graphStatement.getGraphLanguage()),
                GraphOptions.GRAPH_NAME_KEY, PayloadHelper.asBytes(graphStatement.getGraphName()),
                GraphOptions.GRAPH_SOURCE_KEY, PayloadHelper.asBytes(graphStatement.getGraphSource())
        );
    }

    private Map<String, ByteBuffer> buildPayloadFromProperties(String graphAlias, String graphLanguage, String graphName, String graphSource) {
        ImmutableMap.Builder<String, ByteBuffer> builder = ImmutableMap.builder();
        if (graphAlias != null) {
            builder.put(GraphOptions.GRAPH_ALIAS_KEY, PayloadHelper.asBytes(graphAlias));
        }
        if (graphLanguage != null) {
            builder.put(GraphOptions.GRAPH_LANGUAGE_KEY, PayloadHelper.asBytes(graphLanguage));
        }
        if (graphName != null) {
            builder.put(GraphOptions.GRAPH_NAME_KEY, PayloadHelper.asBytes(graphName));
        }
        if (graphSource != null) {
            builder.put(GraphOptions.GRAPH_SOURCE_KEY, PayloadHelper.asBytes(graphSource));
        }
        return builder.build();
    }

}