/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

public class BatchStatementIdempotencyTest extends AbstractBatchIdempotencyTest {

    @Override
    protected TestBatch createBatch() {
        return new TestBatchStatementWrapper();
    }

    static class TestBatchStatementWrapper implements TestBatch {

        private final BatchStatement batch = new BatchStatement();

        @Override
        public void add(RegularStatement statement) {
            batch.add(statement);
        }

        @Override
        public Boolean isIdempotent() {
            return batch.isIdempotent();
        }

        @Override
        public void setIdempotent(boolean idempotent) {
            batch.setIdempotent(idempotent);
        }
    }
}
