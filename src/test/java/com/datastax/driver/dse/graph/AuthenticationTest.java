/*
 *      Copyright (C) 2012-2015 DataStax Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.CCMBridge;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

import static com.datastax.driver.dse.graph.Assertions.assertThat;

public class AuthenticationTest extends CCMGraphTestsSupport {

    @SuppressWarnings("unused")
    public CCMBridge.Builder configureCCM() {
        return super.configureCCM()
                .withCassandraConfiguration("authenticator", "PasswordAuthenticator")
                .withJvmArgs("-Dcassandra.superuser_setup_delay_ms=0");
    }

    @Override
    public Cluster.Builder createClusterBuilder() {
        return super.createClusterBuilder()
                .withAuthProvider(new PlainTextAuthProvider("cassandra", "cassandra"));
    }

    /**
     * Validates that queries can be executed over an authenticated interface.
     *
     * @test_category dse:graph
     */
    @Test(groups = "short", enabled = false)
    public void should_be_able_to_create_vertex() {
        // TODO: Reenable when DSP-8191 fixed.
        GraphResult result = session().executeGraph("g.addV(label, 'person', 'name', name)",
                ImmutableMap.<String, Object>of("name", "andy")).one();
        assertThat(result).asVertex().hasLabel("person").hasProperty("name", "andy");
    }
}
