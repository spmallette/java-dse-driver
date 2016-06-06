/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that supported JSR 310 temporal types can be serialized and deserialized
 * with GraphJsonUtils.
 */
@SuppressWarnings("Since15")
public class Jdk8Jsr310GraphJsonUtilsTest {

    java.time.Instant instant = java.time.Instant.parse("2016-05-12T16:12:23.999Z");

    java.time.ZonedDateTime zonedDateTime = java.time.ZonedDateTime.parse("2016-05-12T16:12:23.999Z");

    java.time.Duration duration = java.time.Duration.parse("P2DT3H4M");

    @Test(groups = "unit")
    public void should_serialize_supported_types() throws Exception {

        GraphJsonUtils jsonUtils = GraphJsonUtils.INSTANCE;

        assertThat(jsonUtils.writeValueAsString(instant)).isEqualTo("\"" + instant + "\"");
        assertThat(jsonUtils.writeValueAsString(zonedDateTime)).isEqualTo("\"" + zonedDateTime + "\"");
        assertThat(jsonUtils.writeValueAsString(duration)).isEqualTo("\"" + duration + "\"");

    }

    @Test(groups = "unit")
    public void should_deserialize_supported_types() throws Exception {

        GraphJsonUtils jsonUtils = GraphJsonUtils.INSTANCE;

        assertThat(jsonUtils.readStringAsTree("\"" + instant + "\"").as(java.time.Instant.class)).isEqualTo(instant);
        assertThat(jsonUtils.readStringAsTree("\"" + zonedDateTime + "\"").as(java.time.ZonedDateTime.class)).isEqualTo(zonedDateTime);
        assertThat(jsonUtils.readStringAsTree("\"" + duration + "\"").as(java.time.Duration.class)).isEqualTo(duration);

    }

}
