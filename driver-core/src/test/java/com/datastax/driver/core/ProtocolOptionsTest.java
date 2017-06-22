/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.testng.annotations.Test;

import static com.datastax.driver.core.Assertions.assertThat;

public class ProtocolOptionsTest extends CCMTestsSupport {

    /**
     * @jira_ticket JAVA-1209
     */
    @Test(groups = "unit")
    public void getProtocolVersion_should_return_null_if_not_connected() {
        Cluster cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
        assertThat(cluster.getConfiguration().getProtocolOptions().getProtocolVersion()).isNull();
    }

    /**
     * @jira_ticket JAVA-1209
     */
    @Test(groups = "short")
    public void getProtocolVersion_should_return_version() throws InterruptedException {
        ProtocolVersion version = cluster().getConfiguration().getProtocolOptions().getProtocolVersion();
        assertThat(version).isNotNull();
    }
}
