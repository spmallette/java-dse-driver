/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphOptionsPayloadTest {

    @Test(groups = "unit")
    public void should_use_default_options_when_none_set() {
        GraphOptions graphOptions = new GraphOptions();

        Map<String, ByteBuffer> expectedPayload = buildPayloadFromProperties(GraphOptions.DEFAULT_GRAPH_LANGUAGE, null, GraphOptions.DEFAULT_GRAPH_SOURCE, null, null);

        Map<String, ByteBuffer> resultPayload = graphOptions.buildPayloadWithDefaults(new SimpleGraphStatement(""));

        assertThat(resultPayload).isEqualTo(expectedPayload);
    }

    @Test(groups = "unit")
    public void should_use_cluster_options_set() {
        GraphOptions graphOptions = new GraphOptions();
        graphOptions.setGraphLanguage("language1");
        graphOptions.setGraphName("name1");
        graphOptions.setGraphSource("source1");
        graphOptions.setGraphReadConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        graphOptions.setGraphWriteConsistencyLevel(ConsistencyLevel.ALL);

        Map<String, ByteBuffer> expectedPayload = buildPayloadFromProperties("language1", "name1", "source1", ConsistencyLevel.LOCAL_QUORUM, ConsistencyLevel.ALL);

        Map<String, ByteBuffer> resultPayload = graphOptions.buildPayloadWithDefaults(new SimpleGraphStatement(""));

        assertThat(resultPayload).isEqualTo(expectedPayload);
    }

    @Test(groups = "unit")
    public void should_use_statement_options_over_cluster_options() {

        GraphOptions graphOptions = new GraphOptions();
        graphOptions.setGraphLanguage("language1");
        graphOptions.setGraphName("name1");
        graphOptions.setGraphSource("source1");
        graphOptions.setGraphReadConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        graphOptions.setGraphWriteConsistencyLevel(ConsistencyLevel.ALL);

        SimpleGraphStatement simpleGraphStatement = new SimpleGraphStatement("");
        simpleGraphStatement.setGraphLanguage("language2");
        simpleGraphStatement.setGraphName("name2");
        simpleGraphStatement.setGraphSource("source2");
        simpleGraphStatement.setGraphReadConsistencyLevel(ConsistencyLevel.ANY);
        simpleGraphStatement.setGraphWriteConsistencyLevel(ConsistencyLevel.EACH_QUORUM);

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
    public void should_allow_null_graph_name_on_cluster() {
        GraphOptions graphOptions = new GraphOptions();
        graphOptions.setGraphLanguage("language1");
        graphOptions.setGraphSource("source1");
        graphOptions.setGraphName(null);

        Map<String, ByteBuffer> expectedPayload = buildPayloadFromProperties("language1", null, "source1", null, null);

        Map<String, ByteBuffer> resultPayload = graphOptions.buildPayloadWithDefaults(new SimpleGraphStatement(""));

        assertThat(resultPayload).isEqualTo(expectedPayload);

    }

    @Test(groups = "unit")
    public void should_force_no_graph_name_if_statement_is_a_system_query() {
        GraphOptions graphOptions = new GraphOptions();
        graphOptions.setGraphLanguage("language1");
        graphOptions.setGraphName("name1");
        graphOptions.setGraphSource("source1");

        SimpleGraphStatement simpleGraphStatement = new SimpleGraphStatement("");
        simpleGraphStatement.setSystemQuery();

        Map<String, ByteBuffer> expectedPayload = buildPayloadFromProperties("language1", null, "source1", null, null);

        Map<String, ByteBuffer> resultPayload = graphOptions.buildPayloadWithDefaults(simpleGraphStatement);

        assertThat(resultPayload).isEqualTo(expectedPayload);
    }

    @Test(groups = "unit")
    public void should_use_native_consistency_and_timestamp() {
        ConsistencyLevel desiredCL = ConsistencyLevel.ALL;
        long desiredTimestamp = 12L;
        SimpleGraphStatement sgt = new SimpleGraphStatement("query");
        sgt.setConsistencyLevel(desiredCL);
        sgt.setDefaultTimestamp(desiredTimestamp);
        Statement st = sgt.unwrap();
        assertThat(st.getConsistencyLevel()).isEqualTo(desiredCL);
        assertThat(st.getDefaultTimestamp()).isEqualTo(desiredTimestamp);
        sgt.set("a", "a");
        st = sgt.unwrap();

        // Assert that calling maybeRebuildCache internally does not erase the consistency and timestamp.
        assertThat(st.getConsistencyLevel()).isEqualTo(desiredCL);
        assertThat(st.getDefaultTimestamp()).isEqualTo(desiredTimestamp);
    }

    private Map<String, ByteBuffer> buildPayloadFromStatement(GraphStatement graphStatement) {
        return ImmutableMap.of(
                GraphOptions.GRAPH_LANGUAGE_KEY, PayloadHelper.asBytes(graphStatement.getGraphLanguage()),
                GraphOptions.GRAPH_NAME_KEY, PayloadHelper.asBytes(graphStatement.getGraphName()),
                GraphOptions.GRAPH_SOURCE_KEY, PayloadHelper.asBytes(graphStatement.getGraphSource()),
                GraphOptions.GRAPH_READ_CONSISTENCY_KEY, PayloadHelper.asBytes(graphStatement.getGraphReadConsistencyLevel().name()),
                GraphOptions.GRAPH_WRITE_CONSISTENCY_KEY, PayloadHelper.asBytes(graphStatement.getGraphWriteConsistencyLevel().name())
        );
    }

    private Map<String, ByteBuffer> buildPayloadFromProperties(String graphLanguage, String graphName, String graphSource, ConsistencyLevel readCL, ConsistencyLevel writeCL) {
        ImmutableMap.Builder<String, ByteBuffer> builder = ImmutableMap.builder();
        if (graphLanguage != null) {
            builder.put(GraphOptions.GRAPH_LANGUAGE_KEY, PayloadHelper.asBytes(graphLanguage));
        }
        if (graphName != null) {
            builder.put(GraphOptions.GRAPH_NAME_KEY, PayloadHelper.asBytes(graphName));
        }
        if (graphSource != null) {
            builder.put(GraphOptions.GRAPH_SOURCE_KEY, PayloadHelper.asBytes(graphSource));
        }
        if (readCL != null) {
            builder.put(GraphOptions.GRAPH_READ_CONSISTENCY_KEY, PayloadHelper.asBytes(readCL.name()));
        }
        if (writeCL != null) {
            builder.put(GraphOptions.GRAPH_WRITE_CONSISTENCY_KEY, PayloadHelper.asBytes(writeCL.name()));
        }
        return builder.build();
    }

}
