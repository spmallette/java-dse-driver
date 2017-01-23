/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.google.common.collect.AbstractIterator;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Uninterruptibles;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;

class DefaultContinuousPagingResult implements ContinuousPagingResult {

    private final RowIterator iterator;
    private final ColumnDefinitions columnDefinitions;

    DefaultContinuousPagingResult(ListenableFuture<AsyncContinuousPagingResult> firstPageFuture) {
        try {
            AsyncContinuousPagingResult result = Uninterruptibles.getUninterruptibly(firstPageFuture);
            this.iterator = new RowIterator(result);
            this.columnDefinitions = result.getColumnDefinitions();
        } catch (ExecutionException e) {
            throw DriverThrowables.propagateCause(e);
        }
    }

    @Override
    public Iterator<Row> iterator() {
        return iterator;
    }

    @Override
    public ColumnDefinitions getColumnDefinitions() {
        return columnDefinitions;
    }

    @Override
    public void cancel() {
        this.iterator.cancel();
    }

    @Override
    public ExecutionInfo getExecutionInfo() {
        return this.iterator.getExecutionInfo();
    }

    private static class RowIterator extends AbstractIterator<Row> {
        private AsyncContinuousPagingResult currentResult;
        private Iterator<Row> currentRows;
        private boolean cancelled;

        private RowIterator(AsyncContinuousPagingResult result) {
            this.currentResult = result;
            this.currentRows = result.currentPage().iterator();
        }

        @Override
        protected Row computeNext() {
            maybeFetchNextResult();
            return currentRows.hasNext() ? currentRows.next() : endOfData();
        }

        private void maybeFetchNextResult() {
            if (!currentRows.hasNext() && !cancelled && !currentResult.isLast()) {
                try {
                    currentResult = Uninterruptibles.getUninterruptibly(currentResult.nextPage());
                    currentRows = currentResult.currentPage().iterator();
                } catch (ExecutionException e) {
                    throw DriverThrowables.propagateCause(e);
                }
            }
        }

        private ExecutionInfo getExecutionInfo() {
            return currentResult.getExecutionInfo();
        }

        private void cancel() {
            currentResult.cancel();
            cancelled = true;
        }
    }
}
