/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.assertj.core.api.AbstractComparableAssert;

import static com.datastax.driver.core.Assertions.assertThat;

public class VersionNumberAssert extends AbstractComparableAssert<VersionNumberAssert, VersionNumber> {

    public VersionNumberAssert(VersionNumber actual) {
        super(actual, VersionNumberAssert.class);
    }

    public VersionNumberAssert hasMajorMinorPatch(int major, int minor, int patch) {
        assertThat(actual.getMajor()).isEqualTo(major);
        assertThat(actual.getMinor()).isEqualTo(minor);
        assertThat(actual.getPatch()).isEqualTo(patch);
        return this;
    }

    public VersionNumberAssert hasDsePatch(int dsePatch) {
        assertThat(actual.getDSEPatch()).isEqualTo(dsePatch);
        return this;
    }

    public VersionNumberAssert hasPreReleaseLabels(String... labels) {
        assertThat(actual.getPreReleaseLabels()).containsExactly(labels);
        return this;
    }

    public VersionNumberAssert hasNoPreReleaseLabels() {
        assertThat(actual.getPreReleaseLabels()).isNull();
        return this;
    }

    public VersionNumberAssert hasBuildLabel(String label) {
        assertThat(actual.getBuildLabel()).isEqualTo(label);
        return this;
    }

    public VersionNumberAssert hasNextStable(String version) {
        assertThat(actual.nextStable()).isEqualTo(VersionNumber.parse(version));
        return this;
    }

    public VersionNumberAssert hasToString(String string) {
        assertThat(actual.toString()).isEqualTo(string);
        return this;
    }
}
