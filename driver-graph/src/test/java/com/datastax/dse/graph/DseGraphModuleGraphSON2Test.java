/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph;

import com.datastax.dse.graph.api.predicates.Geo;
import com.datastax.dse.graph.api.predicates.Search;
import com.datastax.dse.graph.internal.serde.DseGraphModule;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONMapper;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONVersion;
import org.apache.tinkerpop.shaded.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class DseGraphModuleGraphSON2Test {

    private ObjectMapper om = GraphSONMapper.build()
            .version(GraphSONVersion.V2_0)
            .addCustomModule(new DseGraphModule())
            .create().createMapper();

    @Test(groups = "unit")
    public void should_serialize_and_deserialize_geo_inside_cartesian_predicate() throws IOException {
        P<?> predicate = Geo.inside(Geo.point(10, 10), 10);
        assertThat(serDeserCompareObjects(predicate)).isTrue();
    }

    @Test(groups = "unit")
    public void should_serialize_and_deserialize_geo_inside_predicate() throws IOException {
        P<?> predicate = Geo.inside(Geo.point(10, 10), 10, Geo.Unit.DEGREES);
        assertThat(serDeserCompareObjects(predicate)).isTrue();
    }

    @Test(groups = "unit")
    public void should_serialize_and_deserialize_search_token_predicate() throws IOException {
        P<?> predicate = Search.token("token");
        assertThat(serDeserCompareObjects(predicate)).isTrue();
    }

    @Test(groups = "unit")
    public void should_serialize_and_deserialize_search_token_prefix_predicate() throws IOException {
        P<?> predicate = Search.tokenPrefix("tokenPrefix");
        assertThat(serDeserCompareObjects(predicate)).isTrue();
    }

    @Test(groups = "unit")
    public void should_serialize_and_deserialize_search_token_regex_predicate() throws IOException {
        P<?> predicate = Search.tokenRegex("tokenRegex");
        assertThat(serDeserCompareObjects(predicate)).isTrue();
    }

    @Test(groups = "unit")
    public void should_serialize_and_deserialize_search_prefix_predicate() throws IOException {
        P<?> predicate = Search.prefix("prefix");
        assertThat(serDeserCompareObjects(predicate)).isTrue();
    }

    @Test(groups = "unit")
    public void should_serialize_and_deserialize_search_regex_predicate() throws IOException {
        P<?> predicate = Search.regex("regex");
        assertThat(serDeserCompareObjects(predicate)).isTrue();
    }

    @Test(groups = "unit")
    public void should_serialize_and_deserialize_search_phrase_predicate() throws IOException {
        P<?> predicate = Search.phrase("phrase", 2);
        assertThat(serDeserCompareObjects(predicate)).isTrue();
    }

    @Test(groups = "unit")
    public void should_serialize_and_deserialize_search_fuzzy_predicate() throws IOException {
        P<?> predicate = Search.fuzzy("fuzzy", 2);
        assertThat(serDeserCompareObjects(predicate)).isTrue();
    }

    @Test(groups = "unit")
    public void should_serialize_and_deserialize_search_token_fuzzy_predicate() throws IOException {
        P<?> predicate = Search.tokenFuzzy("tokenFuzzy", 2);
        assertThat(serDeserCompareObjects(predicate)).isTrue();
    }

    private boolean serDeserCompareObjects(Object original) throws IOException {
        String jsonString = om.writeValueAsString(original);
        Object readO = om.readValue(jsonString, Object.class);
        return original.equals(readO);
    }
}
