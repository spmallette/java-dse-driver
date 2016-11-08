/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.policies;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Statement;

/**
 * Base class for tests that want to wrap a policy to add some instrumentation.
 * <p/>
 * NB: this is currently only used in tests, but could be provided as a convenience in the production code.
 */
public abstract class DelegatingSpeculativeExecutionPolicy implements SpeculativeExecutionPolicy {
    private final SpeculativeExecutionPolicy delegate;

    protected DelegatingSpeculativeExecutionPolicy(SpeculativeExecutionPolicy delegate) {
        this.delegate = delegate;
    }

    @Override
    public void init(Cluster cluster) {
        delegate.init(cluster);
    }

    @Override
    public SpeculativeExecutionPlan newPlan(String loggedKeyspace, Statement statement) {
        return delegate.newPlan(loggedKeyspace, statement);
    }

    @Override
    public void close() {
        delegate.close();
    }
}
