/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.utils;

import com.datastax.driver.core.MemoryAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UUIDsPIDPropertyInvalidTest {

    private static final Logger logger = Logger.getLogger(UUIDs.class);

    @Test(groups = "isolated")
    public void should_fallback_on_native_call_if_system_property_invalid() {
        // If the com.datastax.driver.PID property is set, but is invalid, it should fallback onto native getpid().
        MemoryAppender appender = new MemoryAppender();
        Level originalLevel = logger.getLevel();
        try {
            logger.setLevel(Level.INFO);
            logger.addAppender(appender);
            String pid = "NOT_A_PID";
            System.setProperty(UUIDs.PID_SYSTEM_PROPERTY, pid);
            UUIDs.timeBased();
            assertThat(appender.get())
                    .containsOnlyOnce(String.format("Incorrect integer specified for PID in System property %s: %s",
                            UUIDs.PID_SYSTEM_PROPERTY, pid))
                    .containsOnlyOnce("PID obtained through native call to getpid()");
        } finally {
            logger.removeAppender(appender);
            logger.setLevel(originalLevel);
        }
    }
}
