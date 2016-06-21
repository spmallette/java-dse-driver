/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse;

import com.datastax.driver.core.Host;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.StatementWrapper;

/**
 * Wraps a statement to indicate a given host as the optimal coordinator to use for the query.
 * <p/>
 * This assumes that the cluster is configured to use {@link HostTargetingLoadBalancingPolicy}; this policy will try to
 * prioritize the host in the query plan if possible (but may still route to a different host if the preferred one is
 * not available).
 */
public class HostTargetingStatement extends StatementWrapper {
    final Host preferredHost;

    /**
     * Builds a new instance.
     *
     * @param wrapped       the wrapped statement.
     * @param preferredHost the preferred coordinator to execute this statement.
     */
    public HostTargetingStatement(Statement wrapped, Host preferredHost) {
        super(wrapped);
        this.preferredHost = preferredHost;
    }
}
