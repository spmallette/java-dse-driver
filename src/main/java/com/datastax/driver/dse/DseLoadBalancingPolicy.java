/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.policies.ChainableLoadBalancingPolicy;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adds DSE-specific load balancing abilities on top of another policy.
 * <p/>
 * Currently, the only special processing performed by this policy is to route graph OLAP queries to the graph analytics
 * master.
 */
public class DseLoadBalancingPolicy implements ChainableLoadBalancingPolicy {

    private static final Logger logger = LoggerFactory.getLogger(DseLoadBalancingPolicy.class);

    private final LoadBalancingPolicy childPolicy;
    private Set<Host> upHosts = Sets.newSetFromMap(new ConcurrentHashMap<Host, Boolean>());

    /**
     * Build a new instance.
     *
     * @param childPolicy the policy to add the DSE-specific behavior to.
     */
    public DseLoadBalancingPolicy(LoadBalancingPolicy childPolicy) {
        this.childPolicy = childPolicy;
    }

    @Override
    public void init(Cluster cluster, Collection<Host> hosts) {
        upHosts.addAll(hosts);
        childPolicy.init(cluster, hosts);
    }

    @Override
    public Iterator<Host> newQueryPlan(String loggedKeyspace, Statement statement) {
        final Host preferredHost = (statement instanceof HostTargetingStatement)
                ? ((HostTargetingStatement) statement).preferredHost
                : null;

        final Iterator<Host> childPlan = childPolicy.newQueryPlan(loggedKeyspace, statement);

        if (preferredHost == null) {
            return childPlan;
        } else if (!upHosts.contains(preferredHost)) {
            logger.debug("Received statement targeted to {}, but it's not available at the moment", preferredHost);
            return childPlan;
        } else {
            logger.debug("Received statement targeted to {}, moving it to the beginning of the query plan", preferredHost);
            return new AbstractIterator<Host>() {
                volatile boolean first = true;

                @Override
                protected Host computeNext() {
                    if (first) {
                        first = false;
                        return preferredHost;
                    } else if (childPlan.hasNext()) {
                        Host next = childPlan.next();
                        if (next.equals(preferredHost)) {
                            // Already returned at the beginning, skip
                            return childPlan.hasNext() ? childPlan.next() : endOfData();
                        } else {
                            return next;
                        }
                    } else {
                        return endOfData();
                    }
                }
            };
        }
    }

    @Override
    public HostDistance distance(Host host) {
        return childPolicy.distance(host);
    }

    @Override
    public void onAdd(Host host) {
        upHosts.add(host);
        childPolicy.onAdd(host);
    }

    @Override
    public void onUp(Host host) {
        upHosts.add(host);
        childPolicy.onUp(host);
    }

    @Override
    public void onDown(Host host) {
        upHosts.remove(host);
        childPolicy.onDown(host);
    }

    @Override
    public void onRemove(Host host) {
        upHosts.remove(host);
        childPolicy.onRemove(host);
    }

    @Override
    public LoadBalancingPolicy getChildPolicy() {
        return childPolicy;
    }

    @Override
    public void close() {
        childPolicy.close();
    }
}
