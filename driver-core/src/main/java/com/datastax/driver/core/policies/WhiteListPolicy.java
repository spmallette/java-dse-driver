/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.policies;

import com.datastax.driver.core.Host;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * A load balancing policy wrapper that ensure that only hosts from a provided
 * white list will ever be returned.
 * <p/>
 * This policy wraps another load balancing policy and will delegate the choice
 * of hosts to the wrapped policy with the exception that only hosts contained
 * in the white list provided when constructing this policy will ever be
 * returned. Any host not in the while list will be considered {@code IGNORED}
 * and thus will not be connected to.
 * <p/>
 * This policy can be useful to ensure that the driver only connects to a
 * predefined set of hosts. Keep in mind however that this policy defeats
 * somewhat the host auto-detection of the driver. As such, this policy is only
 * useful in a few special cases or for testing, but is not optimal in general.
 * If all you want to do is limiting connections to hosts of the local
 * data-center then you should use DCAwareRoundRobinPolicy and *not* this policy
 * in particular.
 *
 * @see HostFilterPolicy
 */
public class WhiteListPolicy extends HostFilterPolicy {

    /**
     * Creates a new policy that wraps the provided child policy but only "allows" hosts
     * from the provided while list.
     *
     * @param childPolicy the wrapped policy.
     * @param whiteList   the white listed hosts. Only hosts from this list may get connected
     *                    to (whether they will get connected to or not depends on the child policy).
     */
    public WhiteListPolicy(LoadBalancingPolicy childPolicy, Collection<InetSocketAddress> whiteList) {
        super(childPolicy, buildPredicate(whiteList));
    }

    private static Predicate<Host> buildPredicate(Collection<InetSocketAddress> whiteList) {
        final ImmutableSet<InetSocketAddress> hosts = ImmutableSet.copyOf(whiteList);
        return new Predicate<Host>() {
            @Override
            public boolean apply(Host host) {
                return hosts.contains(host.getSocketAddress());
            }
        };
    }

}
