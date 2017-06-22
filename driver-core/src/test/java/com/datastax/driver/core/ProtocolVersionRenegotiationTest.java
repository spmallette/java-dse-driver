/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.datastax.driver.core.exceptions.UnsupportedProtocolVersionException;
import com.datastax.driver.core.utils.DseVersion;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.datastax.driver.core.ProtocolVersion.V1;
import static org.assertj.core.api.Assertions.assertThat;

public class ProtocolVersionRenegotiationTest extends CCMTestsSupport {

    private ProtocolVersion bestProtocolVersion;

    @BeforeMethod(groups = "short")
    public void setUp() {
        bestProtocolVersion = ccm().getProtocolVersion();
    }

    /**
     * @jira_ticket JAVA-1367
     */
    @Test(groups = "short")
    public void should_negotiate_when_no_version_provided() throws Exception {
        Cluster cluster = connectWithoutVersion();
        assertThat(actualProtocolVersion(cluster)).isEqualTo(bestProtocolVersion);
    }

    /**
     * @jira_ticket JAVA-1367
     */
    @Test(groups = "short")
    public void should_succeed_when_version_provided_and_matches() throws Exception {
        Cluster cluster = connectWithVersion(bestProtocolVersion);
        assertThat(actualProtocolVersion(cluster)).isEqualTo(bestProtocolVersion);
    }

    /**
     * @jira_ticket JAVA-1367
     */
    @Test(groups = "short")
    @DseVersion("5.1.0")
    public void should_fail_when_version_provided_and_too_low() throws Exception {
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
