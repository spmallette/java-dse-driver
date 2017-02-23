/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.stress;

import com.datastax.driver.core.*;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.util.Iterator;

public abstract class QueryGenerator implements Iterator<QueryGenerator.Request> {

    protected final int iterations;

    protected QueryGenerator(int iterations) {
        this.iterations = iterations;
    }

    public abstract int currentIteration();

    public int totalIterations() {
        return iterations;
    }

    public interface Builder {
        public String name();

        public OptionParser addOptions(OptionParser parser);

        public void prepare(OptionSet options, Session session);

        public QueryGenerator create(int id, int iterations, OptionSet options, Session session);
    }

    public interface Request {

        public ResultSet execute(Session session);

        public ResultSetFuture executeAsync(Session session);

        public static class SimpleQuery implements Request {

            private final Statement statement;

            public SimpleQuery(Statement statement) {
                this.statement = statement;
            }

            @Override
            public ResultSet execute(Session session) {
                return session.execute(statement);
            }

            @Override
            public ResultSetFuture executeAsync(Session session) {
                return session.executeAsync(statement);
            }
        }

        public static class PreparedQuery implements Request {

            private final BoundStatement query;

            public PreparedQuery(BoundStatement query) {
                this.query = query;
            }

            @Override
            public ResultSet execute(Session session) {
                return session.execute(query);
            }

            @Override
            public ResultSetFuture executeAsync(Session session) {
                return session.executeAsync(query);
            }
        }
    }
}
