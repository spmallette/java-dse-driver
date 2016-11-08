/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.policies;

import com.datastax.driver.core.CCMConfig;
import com.datastax.driver.core.CCMTestsSupport;
import com.datastax.driver.core.Cluster;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CCMConfig(createSession = false)
public class CloseableLoadBalancingPolicyTest extends CCMTestsSupport {

    private CloseMonitoringPolicy policy;

    @Test(groups = "short")
    public void should_be_invoked_at_shutdown() {
        try {
            cluster().connect();
            cluster().close();
        } finally {
            assertThat(policy.wasClosed).isTrue();
        }
    }

    @Override
    public Cluster.Builder createClusterBuilder() {
        policy = new CloseMonitoringPolicy(Policies.defaultLoadBalancingPolicy());
        return Cluster.builder()
                .addContactPoints(getContactPoints().get(0))
                .withLoadBalancingPolicy(policy);
    }

    static class CloseMonitoringPolicy extends DelegatingLoadBalancingPolicy {

        volatile boolean wasClosed = false;

        public CloseMonitoringPolicy(LoadBalancingPolicy delegate) {
            super(delegate);
        }

        @Override
        public void close() {
            wasClosed = true;
        }
    }
}
