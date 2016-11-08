/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.stress;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.DriverException;
import com.google.common.util.concurrent.Uninterruptibles;

public class BlockingConsumer implements Consumer {

    private final Runner runner = new Runner();

    private final Session session;
    private final QueryGenerator requests;
    private final Reporter reporter;

    public BlockingConsumer(Session session,
                            QueryGenerator requests,
                            Reporter reporter) {
        this.session = session;
        this.requests = requests;
        this.reporter = reporter;
        this.runner.setDaemon(true);
    }

    @Override
    public void start() {
        this.runner.start();
    }

    @Override
    public void join() {
        Uninterruptibles.joinUninterruptibly(this.runner);
    }

    private class Runner extends Thread {

        public Runner() {
            super("Consumer Threads");
        }

        @Override
        public void run() {
            try {
                while (requests.hasNext())
                    handle(requests.next());
            } catch (DriverException e) {
                System.err.println("Error during query: " + e.getMessage());
            }
        }

        protected void handle(QueryGenerator.Request request) {
            Reporter.Context ctx = reporter.newRequest();
            try {
                request.execute(session);
            } finally {
                ctx.done();
            }
        }
    }
}
