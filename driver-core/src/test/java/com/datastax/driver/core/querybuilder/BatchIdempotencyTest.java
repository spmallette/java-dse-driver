/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.querybuilder;

import com.datastax.driver.core.AbstractBatchIdempotencyTest;
import com.datastax.driver.core.RegularStatement;

public class BatchIdempotencyTest extends AbstractBatchIdempotencyTest {

    @Override
    protected AbstractBatchIdempotencyTest.TestBatch createBatch() {
        return new TestBatchWrapper();
    }

    static class TestBatchWrapper implements TestBatch {

        private final Batch batch = QueryBuilder.batch();

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
