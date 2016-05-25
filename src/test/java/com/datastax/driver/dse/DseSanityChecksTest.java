/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse;

import com.datastax.driver.core.MemoryAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DseSanityChecksTest {

    MemoryAppender appender;
    Level level;
    Logger logger;
    String range = "[3.0.1,3.1.0)";

    @BeforeMethod(groups = "unit")
    public void setUp() throws Exception {
        logger = Logger.getLogger(DseSanityChecks.class.getName());
        level = logger.getLevel();
        appender = new MemoryAppender();
        logger.setLevel(Level.WARN);
        logger.addAppender(appender);
    }

    @AfterMethod(groups = "unit")
    public void tearDown() throws Exception {
        logger.setLevel(level);
        logger.removeAppender(appender);
    }

    @Test(groups = "unit")
    public void should_not_validate_version_below_range_lower_bound() throws Exception {
        assertThat(DseSanityChecks.isWithinRange("0.0.0", range)).isFalse();
        assertThat(DseSanityChecks.isWithinRange("3.0", range)).isFalse();
        assertThat(DseSanityChecks.isWithinRange("3.0.0", range)).isFalse();
        assertThat(DseSanityChecks.isWithinRange("3.0.1-SNAPSHOT", range)).isFalse();
        assertThat(DseSanityChecks.isWithinRange("3.0.1-rc1", range)).isFalse();
        assertThat(DseSanityChecks.isWithinRange("3.0.1-rc1-SNAPSHOT", range)).isFalse();
    }

    @Test(groups = "unit")
    public void should_validate_version_within_range() throws Exception {
        assertThat(DseSanityChecks.isWithinRange("3.0.1", range)).isTrue();
        assertThat(DseSanityChecks.isWithinRange("3.0.1.1", range)).isTrue();
        assertThat(DseSanityChecks.isWithinRange("3.0.2", range)).isTrue();
        assertThat(DseSanityChecks.isWithinRange("3.0.999", range)).isTrue();
        assertThat(DseSanityChecks.isWithinRange("3.0.999-SNAPSHOT", range)).isTrue();
    }

    @Test(groups = "unit")
    public void should_not_validate_version_above_range_upper_bound() throws Exception {
        assertThat(DseSanityChecks.isWithinRange("3.1.0", range)).isFalse();
        assertThat(DseSanityChecks.isWithinRange("3.2.0", range)).isFalse();
        // these are theoretically below the upper bound but we consider them as "already released"
        assertThat(DseSanityChecks.isWithinRange("3.1.0-SNAPSHOT", range)).isFalse();
        assertThat(DseSanityChecks.isWithinRange("3.1.0-rc1", range)).isFalse();
        assertThat(DseSanityChecks.isWithinRange("3.1.0-rc1-SNAPSHOT", range)).isFalse();
    }

    @Test(groups = "unit")
    public void should_not_log_warning_when_version_within_range() throws Exception {
        DseSanityChecks.checkRuntimeCoreVersionCompatibility("3.0.1", "[3.0.1,3.1.0)");
        String logs = appender.get();
        assertThat(logs).isEmpty();
    }

    @Test(groups = "unit")
    public void should_log_warning_if_version_outside_range() throws Exception {
        DseSanityChecks.checkRuntimeCoreVersionCompatibility("3.1.0", "[3.0.1,3.1.0)");
        String logs = appender.get();
        assertThat(logs).contains(
                "Detected incompatible core driver version: 3.1.0. " +
                        "Compatible core driver versions should be in the range [3.0.1,3.1.0).");
    }

}
