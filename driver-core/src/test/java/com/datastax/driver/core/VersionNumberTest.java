/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.testng.annotations.Test;

import static com.datastax.driver.core.Assertions.assertThat;

public class VersionNumberTest {

    @Test(groups = "unit")
    public void should_parse_release_version() {
        assertThat(VersionNumber.parse("1.2.19"))
                .hasMajorMinorPatch(1, 2, 19)
                .hasDsePatch(-1)
                .hasNoPreReleaseLabels()
                .hasBuildLabel(null)
                .hasNextStable("1.2.19")
                .hasToString("1.2.19");
    }

    @Test(groups = "unit")
    public void should_parse_release_without_patch() {
        assertThat(VersionNumber.parse("1.2"))
                .hasMajorMinorPatch(1, 2, 0);
    }

    @Test(groups = "unit")
    public void should_parse_pre_release_version() {
        assertThat(VersionNumber.parse("1.2.0-beta1-SNAPSHOT"))
                .hasMajorMinorPatch(1, 2, 0)
                .hasDsePatch(-1)
                .hasPreReleaseLabels("beta1", "SNAPSHOT")
                .hasBuildLabel(null)
                .hasToString("1.2.0-beta1-SNAPSHOT")
                .hasNextStable("1.2.0");
    }

    @Test(groups = "unit")
    public void should_allow_tilde_as_first_pre_release_delimiter() {
        assertThat(VersionNumber.parse("1.2.0~beta1-SNAPSHOT"))
                .hasMajorMinorPatch(1, 2, 0)
                .hasDsePatch(-1)
                .hasPreReleaseLabels("beta1", "SNAPSHOT")
                .hasBuildLabel(null)
                .hasToString("1.2.0-beta1-SNAPSHOT")
                .hasNextStable("1.2.0");
    }

    @Test(groups = "unit")
    public void should_parse_dse_patch() {
        assertThat(VersionNumber.parse("1.2.19.2-SNAPSHOT"))
                .hasMajorMinorPatch(1, 2, 19)
                .hasDsePatch(2)
                .hasToString("1.2.19.2-SNAPSHOT")
                .hasNextStable("1.2.19.2");
    }

    @Test(groups = "unit")
    public void should_order_versions() {
        // by component
        assertOrder("1.2.0", "2.0.0", -1);
        assertOrder("2.0.0", "2.1.0", -1);
        assertOrder("2.0.1", "2.0.2", -1);
        assertOrder("2.0.1.1", "2.0.1.2", -1);

        // shortened vs. longer version
        assertOrder("2.0", "2.0.0", 0);
        assertOrder("2.0", "2.0.1", -1);

        // any DSE version is higher than no DSE version
        assertOrder("2.0.0", "2.0.0.0", -1);
        assertOrder("2.0.0", "2.0.0.1", -1);

        // pre-release vs. release
        assertOrder("2.0.0-beta1", "2.0.0", -1);
        assertOrder("2.0.0-SNAPSHOT", "2.0.0", -1);
        assertOrder("2.0.0-beta1-SNAPSHOT", "2.0.0", -1);

        // pre-release vs. pre-release
        assertOrder("2.0.0-a-b-c", "2.0.0-a-b-d", -1);
        assertOrder("2.0.0-a-b-c", "2.0.0-a-b-c-d", -1);

        // build number ignored
        assertOrder("2.0.0+build01", "2.0.0+build02", 0);
    }

    private void assertOrder(String version1, String version2, int expected) {
        assertThat(VersionNumber.parse(version1).compareTo(VersionNumber.parse(version2))).isEqualTo(expected);
    }
}
