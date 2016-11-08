/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

/**
 * {@link Metrics} options.
 */
public class MetricsOptions {

    private final boolean metricsEnabled;
    private final boolean jmxEnabled;

    /**
     * Creates a new {@code MetricsOptions} object with default values (metrics enabled, JMX reporting enabled).
     */
    public MetricsOptions() {
        this(true, true);
    }

    /**
     * Creates a new {@code MetricsOptions} object.
     *
     * @param jmxEnabled whether to enable JMX reporting or not.
     */
    public MetricsOptions(boolean enabled, boolean jmxEnabled) {
        this.metricsEnabled = enabled;
        this.jmxEnabled = jmxEnabled;
    }

    /**
     * Returns whether metrics are enabled.
     *
     * @return whether metrics are enabled.
     */
    public boolean isEnabled() {
        return metricsEnabled;
    }

    /**
     * Returns whether JMX reporting is enabled.
     *
     * @return whether JMX reporting is enabled.
     */
    public boolean isJMXReportingEnabled() {
        return jmxEnabled;
    }
}
