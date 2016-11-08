/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.datastax.driver.core.exceptions.ReadTimeoutException;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ClusterWidePercentileTrackerTest
        extends PercentileTrackerTest<ClusterWidePercentileTracker.Builder, ClusterWidePercentileTracker> {

    @Test(groups = "unit")
    public void should_track_all_measurements_for_cluster() {
        // given - a cluster wide percentile tracker.
        Cluster cluster0 = mock(Cluster.class);
        ClusterWidePercentileTracker tracker = builder()
                .withInterval(1, TimeUnit.SECONDS)
                .withMinRecordedValues(100).build();
        tracker.onRegister(cluster0);

        List<Host> hosts = Lists.newArrayList(mock(Host.class), mock(Host.class), mock(Host.class));
        List<Statement> statements = Lists.newArrayList(mock(Statement.class), mock(Statement.class));
        List<Exception> exceptions = Lists.newArrayList(new Exception(), null, new ReadTimeoutException(ConsistencyLevel.ANY, 1, 1, true), null, null);

        // when - recording latencies over a linear progression with varying hosts, statements and exceptions.
        for (int i = 0; i < 100; i++) {
            tracker.update(
                    hosts.get(i % hosts.size()),
                    statements.get(i % statements.size()),
                    exceptions.get(i % exceptions.size()), TimeUnit.NANOSECONDS.convert(i + 1, TimeUnit.MILLISECONDS));
        }
        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);

        // then - the resulting tracker's percentiles should represent that linear progression. (x percentile == x)
        for (int i = 1; i <= 99; i++) {
            long latencyAtPct = tracker.getLatencyAtPercentile(null, null, null, i);
            assertThat(latencyAtPct).isEqualTo(i);
        }
    }

    @Override
    public ClusterWidePercentileTracker.Builder builder() {
        return ClusterWidePercentileTracker.builder(defaultMaxLatency);
    }
}
