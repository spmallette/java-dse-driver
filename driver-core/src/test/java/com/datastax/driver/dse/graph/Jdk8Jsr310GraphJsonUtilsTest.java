/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.dse.IgnoreJDK6Requirement;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that supported JSR 310 temporal types can be serialized and deserialized
 * with GraphJsonUtils.
 */
@IgnoreJDK6Requirement
@SuppressWarnings("Since15")
public class Jdk8Jsr310GraphJsonUtilsTest {

    java.time.Instant instant = java.time.Instant.parse("2016-05-12T16:12:23.999Z");

    java.time.Duration duration = java.time.Duration.parse("P2DT3H4M");

    @Test(groups = "unit")
    public void should_serialize_supported_types_graphson_1_0() throws Exception {
        assertThat(GraphJsonUtils.writeValueAsString(instant)).isEqualTo("\"" + instant + "\"");
        assertThat(GraphJsonUtils.writeValueAsString(duration)).isEqualTo("\"" + duration + "\"");
    }

    @Test(groups = "unit")
    public void should_deserialize_supported_types_graphson_1_0() throws Exception {
        assertThat(GraphJsonUtils.readStringAsTree("\"" + instant + "\"").as(java.time.Instant.class)).isEqualTo(instant);
        assertThat(GraphJsonUtils.readStringAsTree("\"" + duration + "\"").as(java.time.Duration.class)).isEqualTo(duration);
    }

    @Test(groups = "unit")
    public void should_deserialize_supported_types_graphson_2_0() throws Exception {
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\":\"gx:Instant\",\"@value\":\"" + instant + "\"}").as(java.time.Instant.class)).isEqualTo(instant);
        assertThat(GraphJsonUtils.readStringAsTreeGraphson20("{\"@type\":\"gx:Duration\",\"@value\":\"" + duration + "\"}").as(java.time.Duration.class)).isEqualTo(duration);
    }
}
