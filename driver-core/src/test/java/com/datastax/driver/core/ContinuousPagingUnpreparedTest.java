/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.CCMDseTestsSupport;
import com.datastax.driver.dse.DseCluster;
import com.google.common.collect.Lists;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;


@DseVersion("5.1.0")
@CCMConfig(numberOfNodes = 2)
public class ContinuousPagingUnpreparedTest extends CCMDseTestsSupport {

    public static final String KEY = "k";

    @Override
    public void onTestContextInitialized() {
        execute("CREATE TABLE test (k text, v int, PRIMARY KEY (k, v))");
        for (int i = 0; i < 100; i++) {
            execute(String.format("INSERT INTO test (k, v) VALUES ('%s', %d)", KEY, i));
        }
    }

    @Override
    public DseCluster.Builder createClusterBuilder() {
        return super.createClusterBuilder()
                // set to false so PreparedStatement is only done on 1 node.
                .withQueryOptions(new QueryOptions().setPrepareOnAllHosts(false))
                .withLoadBalancingPolicy(new StatementTypeRoutingLoadBalancingPolicy());
    }

    private ContinuousPagingSession cSession() {
        return (ContinuousPagingSession) super.session();
    }

    /**
     * Validates that if a {@link BoundStatement} is executed using continuous paging that if the DSE node that handles
     * the request does not have knowledge of the associated {@link PreparedStatement} and returns an UNPREPARED
     * response that the driver handles this and prepares the query on that host and tries again.
     *
     * @jira_ticket JAVA-1406
     * @test_category queries
     */
    @Test(groups = "long")
    public void should_reprepare_query_on_unprepared_response() {
        // Capture messages emitted by the logger, this is an indirect way of ensuring an unprepared
        // response was received and handled.
        Logger logger = Logger.getLogger(MultiResponseRequestHandler.class);
        Level originalLevel = logger.getLevel();
        MemoryAppender logs = new MemoryAppender();

        try {
            logger.addAppender(logs);
            logger.setLevel(Level.INFO);

            // Prepare the query, this should be done on host0.
            PreparedStatement prepared = cSession().prepare("select * from test where k = ?");

            // Execute the query using continuous paging, this should be done on host1 where the query isn't
            // prepared.
            ContinuousPagingOptions options = ContinuousPagingOptions.builder()
                    .withPageSize(10, ContinuousPagingOptions.PageUnit.ROWS)
                    .build();
            ContinuousPagingResult result = cSession().executeContinuously(prepared.bind(KEY), options);
            assertThat(result).hasSize(100);

            // Validate that an unprepared response was received, this affirms that an unprepare was sent and handled.
            assertThat(logs.get()).contains("Query select * from test where k = ? is not prepared")
                    .contains("preparing before retrying executing");
        } finally {
            logger.removeAppender(logs);
            logger.setLevel(originalLevel);
        }
    }

    /**
     * A load balancing policy that assumes two hosts.  All non-bound statements get executed on host0, whle bound
     * statements get executed on host1.  This ensures that when a bound statement is executed it is done on a
     * different host than where it was prepared.
     */
    static class StatementTypeRoutingLoadBalancingPolicy implements LoadBalancingPolicy {

        private Host host0;
        private Host host1;

        @Override
        public void init(Cluster cluster, Collection<Host> hosts) {
            Iterator<Host> it = hosts.iterator();
            host0 = it.next();
            host1 = it.next();
        }

        @Override
        public HostDistance distance(Host host) {
            return HostDistance.LOCAL;
        }

        @Override
        public Iterator<Host> newQueryPlan(String loggedKeyspace, Statement statement) {
            // send bound statements to host1, otherwise host0, ensures the node the query is prepared
            // on is different than the one the query is executed on.
            Host host = statement instanceof BoundStatement ? host1 : host0;
            return Lists.newArrayList(host).iterator();
        }

        @Override
        public void onAdd(Host host) {
            // no op
        }

        @Override
        public void onUp(Host host) {
            // no op
        }

        @Override
        public void onDown(Host host) {
            // no op
        }

        @Override
        public void onRemove(Host host) {
            // no op
        }

        @Override
        public void close() {
            // no op
        }
    }
}
