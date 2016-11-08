/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.datastax.driver.core.utils.Bytes;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.util.Set;

import static com.datastax.driver.core.Assertions.assertThat;

@CCMConfig(
        // force the initial token to a non-min value to validate that the single range will always be ]minToken, minToken]
        config = "initial_token:1",
        clusterProvider = "createClusterBuilderNoDebouncing"
)
public class SingleTokenIntegrationTest extends CCMTestsSupport {

    /**
     * JAVA-684: Empty TokenRange returned in a one token cluster
     */
    @Test(groups = "short")
    public void should_return_single_non_empty_range_when_cluster_has_one_single_token() {
        cluster().manager.controlConnection.refreshNodeListAndTokenMap();
        Metadata metadata = cluster().getMetadata();
        Set<TokenRange> tokenRanges = metadata.getTokenRanges();
        assertThat(tokenRanges).hasSize(1);
        TokenRange tokenRange = tokenRanges.iterator().next();
        assertThat(tokenRange)
                .startsWith(Token.M3PToken.FACTORY.minToken())
                .endsWith(Token.M3PToken.FACTORY.minToken())
                .isNotEmpty()
                .isNotWrappedAround();

        Set<Host> hostsForRange = metadata.getReplicas(keyspace, tokenRange);
        Host host1 = TestUtils.findHost(cluster(), 1);
        assertThat(hostsForRange).containsOnly(host1);

        ByteBuffer randomPartitionKey = Bytes.fromHexString("0xCAFEBABE");
        Set<Host> hostsForKey = metadata.getReplicas(keyspace, randomPartitionKey);
        assertThat(hostsForKey).containsOnly(host1);

        Set<TokenRange> rangesForHost = metadata.getTokenRanges(keyspace, host1);
        assertThat(rangesForHost).containsOnly(tokenRange);
    }
}
