/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@CCMConfig(createSession = false)
public class PoolingOptionsIntegrationTest extends CCMTestsSupport {

    private ThreadPoolExecutor executor;

    @Override
    public Cluster.Builder createClusterBuilder() {
        executor = spy(new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>()));
        PoolingOptions poolingOptions = new PoolingOptions();
        poolingOptions.setInitializationExecutor(executor);
        return Cluster.builder().withPoolingOptions(poolingOptions);
    }

    @AfterMethod(groups = "short")
    public void shutdownExecutor() {
        if (executor != null)
            executor.shutdown();
    }

    /**
     * <p>
     * Validates that if a custom executor is provided via {@link PoolingOptions#setInitializationExecutor} that it
     * is used to create and tear down connections.
     * </p>
     *
     * @test_category connection:connection_pool
     * @expected_result executor is used and successfully able to connect and tear down connections.
     * @jira_ticket JAVA-692
     * @since 2.0.10, 2.1.6
     */
    @Test(groups = "short")
    public void should_be_able_to_use_custom_initialization_executor() {
        cluster().init();
        // Ensure executor used.
        verify(executor, atLeastOnce()).execute(any(Runnable.class));

        // Reset invocation count.
        reset();

        Session session = cluster().connect();

        // Ensure executor used again to establish core connections.
        verify(executor, atLeastOnce()).execute(any(Runnable.class));

        // Expect core connections + control connection.
        assertThat(cluster().getMetrics().getOpenConnections().getValue()).isEqualTo(
                TestUtils.numberOfLocalCoreConnections(cluster()) + 1);

        reset();

        session.close();

        // Executor should have been used to close connections associated with the session.
        verify(executor, atLeastOnce()).execute(any(Runnable.class));

        // Only the control connection should remain.
        assertThat(cluster().getMetrics().getOpenConnections().getValue()).isEqualTo(1);
    }
}
