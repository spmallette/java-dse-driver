/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.testng.annotations.Test;

public class ExtendedPeerCheckDisabledTest {

    /**
     * Validates that if the com.datastax.driver.EXTENDED_PEER_CHECK system property is set to false that a peer
     * with null values for host_id, data_center, rack, tokens is not ignored.
     *
     * @test_category host:metadata
     * @jira_ticket JAVA-852
     * @since 2.1.10
     */
    @Test(groups = "isolated", dataProvider = "disallowedNullColumnsInPeerData", dataProviderClass = ControlConnectionTest.class)
    @CCMConfig(createCcm = false)
    public void should_use_peer_if_extended_peer_check_is_disabled(String columns) {
        System.setProperty("com.datastax.driver.EXTENDED_PEER_CHECK", "false");
        ControlConnectionTest.run_with_null_peer_info(columns, true);
    }
}
