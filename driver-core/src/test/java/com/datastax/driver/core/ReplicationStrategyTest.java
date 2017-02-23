/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ReplicationStrategyTest {

    @Test(groups = "unit")
    public void createSimpleReplicationStrategyTest() throws Exception {
        ReplicationStrategy strategy = ReplicationStrategy.create(
                ImmutableMap.<String, String>builder()
                        .put("class", "SimpleStrategy")
                        .put("replication_factor", "3")
                        .build());

        assertNotNull(strategy);
        assertTrue(strategy instanceof ReplicationStrategy.SimpleStrategy);
    }

    @Test(groups = "unit")
    public void createNetworkTopologyStrategyTest() throws Exception {
        ReplicationStrategy strategy = ReplicationStrategy.create(
                ImmutableMap.<String, String>builder()
                        .put("class", "NetworkTopologyStrategy")
                        .put("dc1", "2")
                        .put("dc2", "2")
                        .build());

        assertNotNull(strategy);
        assertTrue(strategy instanceof ReplicationStrategy.NetworkTopologyStrategy);
    }

    @Test(groups = "unit")
    public void createSimpleReplicationStrategyWithoutFactorTest() throws Exception {
        ReplicationStrategy strategy = ReplicationStrategy.create(
                ImmutableMap.<String, String>builder()
                        .put("class", "SimpleStrategy")
                                //no replication_factor
                        .build());

        assertNull(strategy);
    }

    @Test(groups = "unit")
    public void createUnknownStrategyTest() throws Exception {
        ReplicationStrategy strategy = ReplicationStrategy.create(
                ImmutableMap.<String, String>builder()
                        //no such strategy
                        .put("class", "FooStrategy")
                        .put("foo_factor", "3")
                        .build());

        assertNull(strategy);
    }

    @Test(groups = "unit")
    public void createUnspecifiedStrategyTest() throws Exception {
        ReplicationStrategy strategy = ReplicationStrategy.create(
                ImmutableMap.<String, String>builder()
                        //nothing useful is set
                        .put("foo", "bar")
                        .build());

        assertNull(strategy);
    }
}
