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
 * This is used in concert with {@link DseLoadBalancingPolicy} to target graph OLAP queries to the graph analytics
 * master.
 */
class HostTargetingStatement extends StatementWrapper {
    final Host preferredHost;

    HostTargetingStatement(Statement wrapped, Host preferredHost) {
        super(wrapped);
        this.preferredHost = preferredHost;
    }
}
