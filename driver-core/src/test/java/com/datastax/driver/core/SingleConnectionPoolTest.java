/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.datastax.driver.core.utils.CassandraVersion;
import org.testng.annotations.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testng.Assert.fail;

@CassandraVersion(major = 2.1)
public class SingleConnectionPoolTest extends CCMTestsSupport {

    @Test(groups = "short")
    public void should_throttle_requests() {
        // Throttle to a very low value. Even a single thread can generate a higher throughput.
        final int maxRequests = 10;
        cluster().getConfiguration().getPoolingOptions()
                .setMaxRequestsPerConnection(HostDistance.LOCAL, maxRequests);

        // Track in flight requests in a dedicated thread every second
        final AtomicBoolean excessInflightQueriesSpotted = new AtomicBoolean(false);
        final Host host = cluster().getMetadata().getHost(ccm().addressOfNode(1));
        ScheduledExecutorService openConnectionsWatcherExecutor = Executors.newScheduledThreadPool(1);
        final Runnable openConnectionsWatcher = new Runnable() {
            @Override
            public void run() {
                int inFlight = session().getState().getInFlightQueries(host);
                if (inFlight > maxRequests)
                    excessInflightQueriesSpotted.set(true);
            }
        };
        openConnectionsWatcherExecutor.scheduleAtFixedRate(openConnectionsWatcher, 200, 200, TimeUnit.MILLISECONDS);

        // Generate the load
        for (int i = 0; i < 10000; i++)
            session().executeAsync("SELECT release_version FROM system.local");

        openConnectionsWatcherExecutor.shutdownNow();
        if (excessInflightQueriesSpotted.get()) {
            fail("Inflight queries exceeded the limit");
        }
    }
}
