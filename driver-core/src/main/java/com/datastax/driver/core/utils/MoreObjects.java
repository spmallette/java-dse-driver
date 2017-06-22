/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.utils;

import java.util.Arrays;

/**
 * Driver-specific implementation of utility object methods.
 * <p>
 * They are available in some versions of Java/Guava, but not across all versions ranges supported by the driver, hence
 * the custom implementation.
 */
public class MoreObjects {
    public static boolean equal(Object first, Object second) {
        return (first == second) || (first != null && first.equals(second));
    }

    public static int hashCode(Object... objects) {
        return Arrays.hashCode(objects);
    }
}
