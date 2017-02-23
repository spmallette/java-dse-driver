/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows overriding internal settings via system properties.
 * <p/>
 * This is generally reserved for tests or "expert" usage.
 */
class SystemProperties {
    private static final Logger logger = LoggerFactory.getLogger(SystemProperties.class);

    static int getInt(String key, int defaultValue) {
        String stringValue = System.getProperty(key);
        if (stringValue == null) {
            logger.debug("{} is undefined, using default value {}", key, defaultValue);
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(stringValue);
            logger.info("{} is defined, using value {}", key, value);
            return value;
        } catch (NumberFormatException e) {
            logger.warn("{} is defined but could not parse value {}, using default value {}", key, stringValue, defaultValue);
            return defaultValue;
        }
    }

    static boolean getBoolean(String key, boolean defaultValue) {
        String stringValue = System.getProperty(key);
        if (stringValue == null) {
            logger.debug("{} is undefined, using default value {}", key, defaultValue);
            return defaultValue;
        }
        try {
            boolean value = Boolean.parseBoolean(stringValue);
            logger.info("{} is defined, using value {}", key, value);
            return value;
        } catch (NumberFormatException e) {
            logger.warn("{} is defined but could not parse value {}, using default value {}", key, stringValue, defaultValue);
            return defaultValue;
        }
    }
}
