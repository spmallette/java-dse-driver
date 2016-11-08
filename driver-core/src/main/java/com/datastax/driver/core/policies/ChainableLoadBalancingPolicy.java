/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.policies;

/**
 * A load balancing policy that wraps another policy.
 */
public interface ChainableLoadBalancingPolicy extends LoadBalancingPolicy {
    /**
     * Returns the child policy.
     *
     * @return the child policy.
     */
    LoadBalancingPolicy getChildPolicy();
}
