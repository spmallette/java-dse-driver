/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse;

import com.datastax.driver.core.Configuration;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.Policies;
import com.datastax.driver.dse.graph.GraphOptions;

/**
 * The configuration of a {@link DseCluster}.
 * <p/>
 * This class extends the CQL driver's {@link Configuration} to add DSE-specific options.
 */
public class DseConfiguration extends Configuration {

    private final GraphOptions graphOptions;

    DseConfiguration(Configuration toCopy, GraphOptions graphOptions) {
        super(toCopy);
        this.graphOptions = graphOptions;
    }

    /**
     * Returns the default graph options to use for the cluster.
     *
     * @return the default graph options.
     */
    public GraphOptions getGraphOptions() {
        return graphOptions;
    }

    /**
     * Builds an instance of the default load balancing policy used for DSE.
     * <p/>
     * It is the core driver's default policy (as returned by {@link Policies#defaultLoadBalancingPolicy()}), wrapped
     * into a {@link DseLoadBalancingPolicy}.
     *
     * @return the new instance.
     */
    public static LoadBalancingPolicy defaultLoadBalancingPolicy() {
        return new DseLoadBalancingPolicy(Policies.defaultLoadBalancingPolicy());
    }
}
