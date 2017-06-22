/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class DseLoadBalancingPolicyTest {
    @Mock
    LoadBalancingPolicy childPolicy;

    @Mock
    Host host1, host2, host3;

    @Mock
    Cluster cluster;

    DseLoadBalancingPolicy targetingPolicy;

    @BeforeMethod(groups = "unit")
    public void setup() {
        MockitoAnnotations.initMocks(this);

        targetingPolicy = new DseLoadBalancingPolicy(childPolicy);
        targetingPolicy.init(cluster, Lists.newArrayList(host1, host2, host3));
    }

    @Test(groups = "unit")
    public void should_return_child_plan_if_statement_not_targeted() {
        // Given
        setChildPlan(host1, host2, host3);

        // When
        Iterator<Host> plan = targetingPolicy.newQueryPlan("ks", new SimpleStatement("test"));

        // Then
        assertThat(plan).containsExactly(host1, host2, host3);
    }

    @Test(groups = "unit")
    public void should_move_target_host_first_if_statement_targeted() {
        // Given
        setChildPlan(host1, host2, host3);

        // When
        Statement s = new HostTargetingStatement(new SimpleStatement("test"), host2);
        Iterator<Host> plan = targetingPolicy.newQueryPlan("ks", s);

        // Then
        assertThat(plan).containsExactly(host2, host1, host3);
    }

    @Test(groups = "unit")
    public void should_ignore_target_host_if_down() {
        // Given
        targetingPolicy.onDown(host2);
        setChildPlan(host1, host3);

        // When
        Statement s = new HostTargetingStatement(new SimpleStatement("test"), host2);
        Iterator<Host> plan = targetingPolicy.newQueryPlan("ks", s);

        // Then
        assertThat(plan).containsExactly(host1, host3);
    }

    private void setChildPlan(Host... hosts) {
        when(childPolicy.newQueryPlan(anyString(), any(Statement.class))).thenReturn(Iterators.forArray(hosts));
    }
}
