/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.testng.SkipException;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClockFactoryTest {

    final String osName = System.getProperty("os.name");

    @Test(groups = "unit")
    public void should_use_native_clock_on_unix_platforms() {
        // Cowardly assume any non-windows platform will support gettimeofday.  If we find one that doesn't,
        // we will have learned something.
        if (osName.startsWith("Windows")) {
            throw new SkipException("Skipping test for Windows platforms.");
        }
        Clock clock = ClockFactory.newInstance();
        assertThat(clock).isInstanceOf(NativeClock.class);
        assertThat(clock.currentTimeMicros()).isGreaterThan(0);
    }

    @Test(groups = "unit")
    public void should_fallback_on_system_clock_on_windows_platforms() {
        if (!osName.startsWith("Windows")) {
            throw new SkipException("Skipping test for non-Windows platforms.");
        }
        Clock clock = ClockFactory.newInstance();
        assertThat(clock).isInstanceOf(SystemClock.class);
        assertThat(clock.currentTimeMicros()).isGreaterThan(0);
    }
}
