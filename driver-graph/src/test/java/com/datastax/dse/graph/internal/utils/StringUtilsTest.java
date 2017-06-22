/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph.internal.utils;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.AssertJUnit.fail;

public class StringUtilsTest {

    @SuppressWarnings("ConstantConditions")
    @Test(groups = "unit")
    public void test_getOptimalStringAlignmentDistance() throws Exception {

        try {
            StringUtils.getOptimalStringAlignmentDistance(null, "foo");
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        
        try {
            StringUtils.getOptimalStringAlignmentDistance("foo", null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        assertThat(StringUtils.getOptimalStringAlignmentDistance("a", "ab")).isEqualTo(1);
        assertThat(StringUtils.getOptimalStringAlignmentDistance("ab", "a")).isEqualTo(1);
        assertThat(StringUtils.getOptimalStringAlignmentDistance("ab", "ac")).isEqualTo(1);
        assertThat(StringUtils.getOptimalStringAlignmentDistance("ab", "ba")).isEqualTo(1);

        // "True" Damerau–Levenshtein distance LD(CA,ABC) = 2 because CA → AC → ABC, but
        // OSA(CA,ABC) = 3: CA → A → AB → ABC
        assertThat(StringUtils.getOptimalStringAlignmentDistance("ca", "abc")).isEqualTo(3);

        assertThat(StringUtils.getOptimalStringAlignmentDistance("", "")).isEqualTo(0);
        assertThat(StringUtils.getOptimalStringAlignmentDistance("", "a")).isEqualTo(1);
        assertThat(StringUtils.getOptimalStringAlignmentDistance("a", "")).isEqualTo(1);

        assertThat(StringUtils.getOptimalStringAlignmentDistance("gift", "gifts")).isEqualTo(1);
        assertThat(StringUtils.getOptimalStringAlignmentDistance("gift", "gif")).isEqualTo(1);
        assertThat(StringUtils.getOptimalStringAlignmentDistance("gift", "gist")).isEqualTo(1);
        assertThat(StringUtils.getOptimalStringAlignmentDistance("gift", "gitf")).isEqualTo(1);
        assertThat(StringUtils.getOptimalStringAlignmentDistance("gift", "igtf")).isEqualTo(2);

    }

}
