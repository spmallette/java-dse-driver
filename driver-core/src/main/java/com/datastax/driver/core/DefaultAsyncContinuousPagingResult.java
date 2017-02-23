/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.List;

class DefaultAsyncContinuousPagingResult implements AsyncContinuousPagingResult {

    private final Iterable<Row> currentPage;
    private final ColumnDefinitions columnDefinitions;
    private final int pageNumber;
    private final boolean isLast;
    private final ExecutionInfo executionInfo;
    private final ContinuousPagingQueue queue;

    private volatile ListenableFuture<AsyncContinuousPagingResult> nextPage;

    DefaultAsyncContinuousPagingResult(Iterable<List<ByteBuffer>> data, final ColumnDefinitions columnDefinitions,
                                       int pageNumber, boolean isLast, ExecutionInfo executionInfo,
                                       final Token.Factory tokenFactory, final ProtocolVersion protocolVersion,
                                       final ContinuousPagingQueue queue) {
        this.currentPage = Iterables.transform(data, new Function<List<ByteBuffer>, Row>() {
            @Override
            public Row apply(List<ByteBuffer> input) {
                return ArrayBackedRow.fromData(columnDefinitions, tokenFactory, protocolVersion, input);
            }
        });
        this.columnDefinitions = columnDefinitions;
        this.pageNumber = pageNumber;
        this.isLast = isLast;
        this.executionInfo = executionInfo;
        this.queue = queue;
    }

    @Override
    public Iterable<Row> currentPage() {
        return currentPage;
    }

    @Override
    public ListenableFuture<AsyncContinuousPagingResult> nextPage() {
        // DCL is fine here, even though we use a lock there is little chance of contention
        ListenableFuture<AsyncContinuousPagingResult> result = this.nextPage;
        if (result == null) {
            synchronized (this) {
                result = nextPage;
                if (result == null) {
                    this.nextPage = result = computeNextPage();
                }
            }
        }
        return result;
    }

    private ListenableFuture<AsyncContinuousPagingResult> computeNextPage() {
        if (isLast) {
            return Futures.immediateFailedFuture(new IllegalStateException("Can't call nextPage() on the last page " +
                    "(use isLast() to check)"));
        } else {
            return queue.dequeueOrCreatePending();
        }
    }

    @Override
    public ColumnDefinitions getColumnDefinitions() {
        return columnDefinitions;
    }

    @Override
    public int pageNumber() {
        return pageNumber;
    }

    @Override
    public boolean isLast() {
        return isLast;
    }

    @Override
    public void cancel() {
        queue.cancel();
    }

    @Override
    public ExecutionInfo getExecutionInfo() {
        return executionInfo;
    }
}
