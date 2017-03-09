/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.google.common.collect.ImmutableMap;
import org.scassandra.Scassandra;
import org.scassandra.http.client.PrimingRequest;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.scassandra.http.client.PrimingRequest.then;

public class RequestHandlerTest {

    @Test(groups = "long")
    public void should_handle_race_between_response_and_cancellation() {
        final Scassandra scassandra = TestUtils.createScassandraServer();
        Cluster cluster = null;

        try {
            // Use a mock server that takes a constant time to reply
            scassandra.start();
            List<Map<String, ?>> rows = Collections.<Map<String, ?>>singletonList(ImmutableMap.of("key", 1));
            scassandra.primingClient().prime(
                    PrimingRequest.queryBuilder()
                            .withQuery("mock query")
                            .withThen(then().withRows(rows).withFixedDelay(10L))
                            .build()
            );

            cluster = Cluster.builder()
                    .addContactPoint(TestUtils.ipOfNode(1))
                    .withPort(scassandra.getBinaryPort())
                    .withPoolingOptions(new PoolingOptions()
                            .setCoreConnectionsPerHost(HostDistance.LOCAL, 1)
                            .setMaxConnectionsPerHost(HostDistance.LOCAL, 1)
                            .setHeartbeatIntervalSeconds(0))
                    .build();

            Session session = cluster.connect();

            // To reproduce, we need to cancel the query exactly when the reply arrives.
            // Run a few queries to estimate how much that will take.
            int samples = 100;
            long start = System.currentTimeMillis();
            for (int i = 0; i < samples; i++) {
                session.execute("mock query");
            }
            long elapsed = System.currentTimeMillis() - start;
            long queryDuration = elapsed / samples;

            // Now run queries and cancel them after that estimated time
            for (int i = 0; i < 2000; i++) {
                ResultSetFuture future = session.executeAsync("mock query");
                try {
                    future.getUninterruptibly(queryDuration, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    future.cancel(true);
                }
            }

            Connection connection = getSingleConnection(session);
            assertThat(connection.inFlight.get()).isEqualTo(0);
        } finally {
            if (cluster != null)
                cluster.close();
            scassandra.stop();
        }
    }

    private Connection getSingleConnection(Session session) {
        HostConnectionPool pool = ((SessionManager) session).pools.values().iterator().next();
        return pool.connections.get(0);
    }
}
