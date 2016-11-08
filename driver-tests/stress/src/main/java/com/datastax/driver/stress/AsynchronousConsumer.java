/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.stress;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.util.concurrent.Uninterruptibles.awaitUninterruptibly;

public class AsynchronousConsumer implements Consumer {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final CountDownLatch shutdownLatch = new CountDownLatch(1);

    private final Session session;
    private final QueryGenerator requests;
    private final Reporter reporter;

    public AsynchronousConsumer(Session session,
                                QueryGenerator requests,
                                Reporter reporter) {
        this.session = session;
        this.requests = requests;
        this.reporter = reporter;
    }

    @Override
    public void start() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                request();
            }
        });
    }

    private void request() {

        if (!requests.hasNext()) {
            shutdown();
            return;
        }

        handle(requests.next());
    }

    @Override
    public void join() {
        awaitUninterruptibly(shutdownLatch);
    }

    protected void handle(QueryGenerator.Request request) {

        final Reporter.Context ctx = reporter.newRequest();

        ResultSetFuture resultSetFuture = request.executeAsync(session);
        Futures.addCallback(resultSetFuture, new FutureCallback<ResultSet>() {
            @Override
            public void onSuccess(final ResultSet result) {
                ctx.done();
                request();
            }

            @Override
            public void onFailure(final Throwable t) {
                // Could do better I suppose
                System.err.println("Error during request: " + t);
                ctx.done();
                request();
            }
        }, executorService);
    }

    protected void shutdown() {
        shutdownLatch.countDown();
    }
}
