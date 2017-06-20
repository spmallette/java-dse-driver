/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

/**
 * Base class for custom {@link Cluster} implementations that wrap another instance (delegate / decorator pattern).
 */
public abstract class DelegatingCluster extends Cluster {

    /**
     * Builds a new instance.
     */
    protected DelegatingCluster() {
    }

    /**
     * Returns the delegate instance where all calls will be forwarded.
     *
     * @return the delegate.
     */
    protected abstract Cluster delegate();

    @Override
    final Manager getManager() {
        return delegate().getManager();
    }

    @Override
    Manager getManager() {
        return delegate().getManager();
    }
}
