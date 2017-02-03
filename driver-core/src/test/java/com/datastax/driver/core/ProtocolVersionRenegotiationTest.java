/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.datastax.driver.core.exceptions.UnsupportedProtocolVersionException;
import org.testng.annotations.Test;

import static com.datastax.driver.core.ProtocolVersion.*;
import static org.assertj.core.api.Assertions.assertThat;

@CreateCCM(CreateCCM.TestMode.PER_METHOD)
public class ProtocolVersionRenegotiationTest extends CCMTestsSupport {

    /**
     * @jira_ticket JAVA-1367
     */
    @Test(groups = "short")
    @CCMConfig(version = "2.1.16", createCluster = false, dse = false)
    public void should_succeed_when_version_provided_and_matches() throws Exception {
        Cluster cluster = connectWithVersion(V3);
        assertThat(actualProtocolVersion(cluster)).isEqualTo(V3);
    }

    /**
     * @jira_ticket JAVA-1367
     */
    @Test(groups = "short")
    @CCMConfig(version = "3.6", createCluster = false, dse = false)
    public void should_fail_when_version_provided_and_too_low_3_6() throws Exception {
        UnsupportedProtocolVersionException e = connectWithUnsupportedVersion(V1);
        assertThat(e.getUnsupportedVersion()).isEqualTo(V1);
        // pre-CASSANDRA-11464: server replies with its own version
        assertThat(e.getServerVersion()).isEqualTo(V4);
    }

    /**
     * @jira_ticket JAVA-1367
     */
    @Test(groups = "short", enabled = false, description = "Disabled until 3.10 is released")
    @CCMConfig(version = "3.10", createCluster = false, dse = false)
    public void should_fail_when_version_provided_and_too_low_3_10() throws Exception {
        UnsupportedProtocolVersionException e = connectWithUnsupportedVersion(V1);
        assertThat(e.getUnsupportedVersion()).isEqualTo(V1);
        // post-CASSANDRA-11464: server replies with client's version
        assertThat(e.getServerVersion()).isEqualTo(V1);
    }

    /**
     * @jira_ticket JAVA-1367
     */
    @Test(groups = "short")
    @CCMConfig(version = "1.2.19", createCluster = false, dse = false)
    public void should_fail_when_version_provided_and_too_high() throws Exception {
        UnsupportedProtocolVersionException e = connectWithUnsupportedVersion(V4);
        assertThat(e.getUnsupportedVersion()).isEqualTo(V4);
        // pre-CASSANDRA-11464: server replies with its own version
        assertThat(e.getServerVersion()).isEqualTo(V1);
    }

    /**
     * @jira_ticket JAVA-1367
     */
    @Test(groups = "short")
    @CCMConfig(version = "2.1.16", createCluster = false, dse = false)
    public void should_fail_when_beta_allowed_and_too_high() throws Exception {
        UnsupportedProtocolVersionException e = connectWithUnsupportedBetaVersion();
        assertThat(e.getUnsupportedVersion()).isEqualTo(V5);
        // pre-CASSANDRA-11464: server replies with its own version
        assertThat(e.getServerVersion()).isEqualTo(V3);
    }

    /**
     * @jira_ticket JAVA-1367
     */
    @Test(groups = "short")
    @CCMConfig(version = "2.1.16", createCluster = false, dse = false)
    public void should_negotiate_when_no_version_provided() throws Exception {
        Cluster cluster = connectWithoutVersion();
        assertThat(actualProtocolVersion(cluster)).isEqualTo(V3);
    }

    /**
     * @jira_ticket JAVA-1367
     */
    @Test(groups = "short",
            enabled = false, description = "Disabled until 5.1.0 is released")
    @CCMConfig(version = "5.1.0", createCluster = false)
    public void should_succeed_when_version_provided_and_matches_dse_5_1() throws Exception {
        Cluster cluster = connectWithVersion(DSE_V1);
        assertThat(actualProtocolVersion(cluster)).isEqualTo(DSE_V1);
    }

    /**
     * @jira_ticket JAVA-1367
     */
    @Test(groups = "short",
            enabled = false, description = "Disabled until 5.1.0 is released")
    @CCMConfig(version = "5.1.0", createCluster = false)
    public void should_negotiate_when_no_version_provided_dse_5_1() throws Exception {
        Cluster cluster = connectWithoutVersion();
        assertThat(actualProtocolVersion(cluster)).isEqualTo(DSE_V1);
    }

    /**
     * @jira_ticket JAVA-1367
     */
    @Test(groups = "short",
            enabled = false, description = "Disabled until 5.1.0 is released")
    @CCMConfig(version = "5.1.0", createCluster = false, dse = true)
    public void should_fail_when_version_provided_and_too_low_dse_5_1() throws Exception {
        UnsupportedProtocolVersionException e = connectWithUnsupportedVersion(V1);
        assertThat(e.getUnsupportedVersion()).isEqualTo(V1);
        // post-CASSANDRA-11464: server replies with client's version
        assertThat(e.getServerVersion()).isEqualTo(V1);
    }

    private UnsupportedProtocolVersionException connectWithUnsupportedVersion(ProtocolVersion version) {
        Cluster cluster = register(Cluster.builder()
                .addContactPoints(getContactPoints())
                .withPort(ccm().getBinaryPort())
                .withProtocolVersion(version)
                .build());
        return initWithUnsupportedVersion(cluster);
    }

    private UnsupportedProtocolVersionException connectWithUnsupportedBetaVersion() {
        Cluster cluster = register(Cluster.builder()
                .addContactPoints(getContactPoints())
                .withPort(ccm().getBinaryPort())
                .allowBetaProtocolVersion()
                .build());
        return initWithUnsupportedVersion(cluster);
    }

    private UnsupportedProtocolVersionException initWithUnsupportedVersion(Cluster cluster) {
        Throwable t = null;
        try {
            cluster.init();
        } catch (Throwable t2) {
            t = t2;
        }
        if (t instanceof UnsupportedProtocolVersionException) {
            return (UnsupportedProtocolVersionException) t;
        } else {
            throw new AssertionError("Expected UnsupportedProtocolVersionException, got " + t);
        }
    }

    private Cluster connectWithVersion(ProtocolVersion version) {
        Cluster cluster = register(Cluster.builder()
                .addContactPoints(getContactPoints())
                .withPort(ccm().getBinaryPort())
                .withProtocolVersion(version)
                .build());
        cluster.init();
        return cluster;
    }

    private Cluster connectWithoutVersion() {
        Cluster cluster = register(Cluster.builder()
                .addContactPoints(getContactPoints())
                .withPort(ccm().getBinaryPort())
                .build());
        cluster.init();
        return cluster;
    }

    private ProtocolVersion actualProtocolVersion(Cluster cluster) {
        return cluster.getConfiguration().getProtocolOptions().getProtocolVersion();
    }

}
