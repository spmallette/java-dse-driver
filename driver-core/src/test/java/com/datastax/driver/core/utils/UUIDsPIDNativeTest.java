/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.utils;

import com.datastax.driver.core.MemoryAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UUIDsPIDNativeTest {

    private static final Logger logger = Logger.getLogger(UUIDs.class);

    @Test(groups = "isolated")
    public void should_obtain_pid_through_native_call() {
        // In the general case the JNR call should *just* work as most systems should support POSIX getpid.
        MemoryAppender appender = new MemoryAppender();
        Level originalLevel = logger.getLevel();
        try {
            logger.setLevel(Level.INFO);
            logger.addAppender(appender);
            UUIDs.timeBased();

            assertThat(appender.get()).containsOnlyOnce("PID obtained through native call to getpid()");
        } finally {
            logger.removeAppender(appender);
            logger.setLevel(originalLevel);
        }
    }
}
