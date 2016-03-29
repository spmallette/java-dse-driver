/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * The result of an {@link ContinuousPagingSession#executeContinuouslyAsync(Statement, ContinuousPagingOptions)
 * asynchronous continuous paging query}.
 * <p/>
 * DSE replies to a continuous query with a stream of response frame. There is one instance of this class for each
 * frame.
 */
public interface AsyncContinuousPagingResult {
    /**
     * Returns an iterable of the rows in the current page.
     * <p/>
     * More results might be available via {@link #nextPage()}.
     */
    Iterable<Row> currentPage();

    /**
     * Indicates if this is the last page in the stream of results.
     */
    boolean isLast();

    /**
     * Returns a future wrapping the next page in the stream of results.
     * <p/>
     * This will return a failed future for the last page; you should always check {@link #isLast()} before calling this
     * method.
     */
    ListenableFuture<AsyncContinuousPagingResult> nextPage();

    /**
     * Returns the definition of the columns returned in this result set.
     */
    ColumnDefinitions getColumnDefinitions();

    /**
     * Returns the page number.
     */
    int pageNumber();

    /**
     * Cancels the continuous query.
     * <p/>
     * The driver will send an additional request to ask DSE to stop sending results. Because that request is
     * asynchronous (and because the driver caches pages locally), there will probably be more pages available (via
     * {@link #nextPage()}) after a call to this method. However, note that the server does not set {@link #isLast()}
     * after a cancellation, so there is no reliable way to detect how many of these remaining pages are left.
     */
    void cancel();

    /**
     * Returns information on the execution of the query, and on the response frame used to build this result.
     * <p/>
     * In particular, it contains the paging state for the current page.
     * <p/>
     * The driver does not support query traces for continuous queries, the corresponding field will always be
     * {@code null}.
     */
    ExecutionInfo getExecutionInfo();
}
