/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.exceptions.OperationTimedOutException;
import com.datastax.driver.core.utils.DseVersion;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@DseVersion("5.0.0")
public class GraphTimeoutsTests extends CCMGraphTestsSupport {

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        executeGraph(GraphFixtures.modern);
    }

    @Test(groups = "short")
    public void should_wait_indefinitely_by_default() {
        long desiredTimeout = 1000L;

        // We could have done with the server's default but it's 30 secs so the test would have taken at least
        // that time. So we simulate a server timeout change.
        session().executeGraph(new SimpleGraphStatement("graph.schema().config().option(\"graph.traversal_sources.drivertest1.evaluation_timeout\").set('" + desiredTimeout + " ms')").setGraphSource("drivertest1"));

        try {
            // The driver should wait indefinitely, but the server should timeout first.
            session().executeGraph(new SimpleGraphStatement("java.util.concurrent.TimeUnit.MILLISECONDS.sleep(35000L);1+1").setGraphSource("drivertest1"));
            fail("The request should have timed out");
        } catch (InvalidQueryException e) {
            assertThat(e.toString()).contains("Script evaluation exceeded", "threshold of " + desiredTimeout + " ms");
        }
    }

    @Test(groups = "short")
    public void should_not_take_into_account_request_timeout_if_more_than_server_timeout() {
        long desiredTimeout = 1000L;
        int clientTimeout = 32000;

        session().executeGraph(new SimpleGraphStatement("graph.schema().config().option(\"graph.traversal_sources.drivertest2.evaluation_timeout\").set('" + desiredTimeout + " ms')").setGraphSource("drivertest2"));

        try {
            // The driver should wait 32 secs, but the server should timeout first.
            session().executeGraph(new SimpleGraphStatement("java.util.concurrent.TimeUnit.MILLISECONDS.sleep(35000L);1+1").setGraphSource("drivertest2").setReadTimeoutMillis(clientTimeout));
            fail("The request should have timed out");
        } catch (InvalidQueryException e) {
            assertThat(e.toString()).contains("Script evaluation exceeded", "threshold of " + desiredTimeout + " ms");
        }
    }

    @Test(groups = "short")
    public void should_take_into_account_request_timeout_if_less_than_server_timeout() {
        long serverTimeout = 10000L;
        int desiredTimeout = 1000;

        // We could have done with the server's default but it's 30 secs so the test would have taken at least
        // that time. Also, we don't want to rely on server's default. So we simulate a server timeout change.
        session().executeGraph(new SimpleGraphStatement("graph.schema().config().option(\"graph.traversal_sources.drivertest3.evaluation_timeout\").set('" + serverTimeout + " ms')").setGraphSource("drivertest3"));

        try {
            // The timeout on the request is lower than what's defined server side, so it should be taken into account.
            session().executeGraph(new SimpleGraphStatement("java.util.concurrent.TimeUnit.MILLISECONDS.sleep(35000L);1+1").setGraphSource("drivertest3").setReadTimeoutMillis(desiredTimeout));
            fail("The request should have timed out");
        } catch (Exception e) {
            // Since server timeout == client timeout, locally concurrency is likely to happen.
            // We cannot know for sure if it will be a Client timeout error, or a Server timeout, and during tests, both happened and not deterministically.
            if (e instanceof InvalidQueryException) {
                assertThat(e.toString()).contains("Script evaluation exceeded", "threshold of " + desiredTimeout + " ms");
            } else {
                assertThat(e).isInstanceOf(OperationTimedOutException.class);
            }
        }
    }
}
