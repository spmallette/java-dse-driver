/*
 *      Copyright (C) 2012-2016 DataStax Inc.
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
 * A load balancing policy that has the ability to use a preferred coordinator to execute statements (provided that they
 * have been wrapped in a {@link HostTargetingStatement}).
 */
public class HostTargetingLoadBalancingPolicy implements ChainableLoadBalancingPolicy {

    private static final Logger logger = LoggerFactory.getLogger(HostTargetingLoadBalancingPolicy.class);

    private final LoadBalancingPolicy childPolicy;
    private Set<Host> upHosts = Sets.newSetFromMap(new ConcurrentHashMap<Host, Boolean>());

    public HostTargetingLoadBalancingPolicy(LoadBalancingPolicy childPolicy) {
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
