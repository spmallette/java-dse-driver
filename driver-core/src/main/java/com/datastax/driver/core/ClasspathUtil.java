/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods and fields to inspect classpath contents.
 */
public class ClasspathUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathUtil.class);

    private static final boolean JSR_310_AVAILABLE;

    static {
        boolean jsr310Available;
        try {
            Class.forName("java.time.Instant");
            jsr310Available = true;
        } catch (LinkageError e) {
            jsr310Available = false;
            LOGGER.warn("JSR 310 could not be loaded", e);
        } catch (ClassNotFoundException e) {
            jsr310Available = false;
        }
        JSR_310_AVAILABLE = jsr310Available;
    }

    /**
     * Returns {@code true} if {@code java.time} API (JSR-310)
     * is available on the classpath, {@code false} otherwise.
     *
     * @return {@code true} if {@code java.time} API
     * is available on the classpath, {@code false} otherwise.
     */
    public static boolean isJavaTimeAvailable() {
        return JSR_310_AVAILABLE;
    }
}
