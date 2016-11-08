/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.google.common.primitives.UnsignedBytes;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * A load balancing policy that sorts hosts on the last byte of the address,
 * so that the query plan is always [host1, host2, host3].
 */
public class SortingLoadBalancingPolicy implements LoadBalancingPolicy {

    final SortedSet<Host> hosts = new ConcurrentSkipListSet<Host>(new Comparator<Host>() {
        @Override
        public int compare(Host host1, Host host2) {
            byte[] address1 = host1.getAddress().getAddress();
            byte[] address2 = host2.getAddress().getAddress();
            return UnsignedBytes.compare(
                    address1[address1.length - 1],
                    address2[address2.length - 1]);
        }
    });

    @Override
    public void init(Cluster cluster, Collection<Host> hosts) {
        this.hosts.addAll(hosts);
    }

    @Override
    public HostDistance distance(Host host) {
        return HostDistance.LOCAL;
    }

    @Override
    public Iterator<Host> newQueryPlan(String loggedKeyspace, Statement statement) {
        return hosts.iterator();
    }

    @Override
    public void onAdd(Host host) {
        onUp(host);
    }

    @Override
    public void onUp(Host host) {
        hosts.add(host);
    }

    @Override
    public void onDown(Host host) {
        hosts.remove(host);
    }

    @Override
    public void onRemove(Host host) {
        onDown(host);
    }

    @Override
    public void close() {/*nothing to do*/}
}
