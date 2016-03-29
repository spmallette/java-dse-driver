/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

/**
 * The result of a {@link ContinuousPagingSession#executeContinuously(Statement, ContinuousPagingOptions)
 * synchronous continuous paging query}.
 * <p/>
 * A synchronous query uses asynchronous calls internally; this object handles the details of performing those calls,
 * and assembling the results in an {@code Iterable}.
 * <p/>
 * Note that {@link #iterator()} will always return the same iterator instance; in other word, this object is only
 * iterable once, it is not possible to restart the iteration from the beginning.
 * <p/>
 * This object is not thread safe, it should only be accessed from the thread that executed the query.
 */
public interface ContinuousPagingResult extends Iterable<Row> {

    /**
     * Returns the definition of the columns returned in this result set.
     */
    ColumnDefinitions getColumnDefinitions();

    /**
     * Cancels the continuous query.
     * <p/>
     * The iteration will stop at the current page in the stream of results; if the server sends more pages after the
     * cancellation (or if more pages were available in the driver's local cache), they will be discarded. Note however
     * that there might still be rows available in the current page.
     * <p/>
     * Therefore, if you plan to resume the iteration later, the correct procedure is:
     * <ul>
     * <li>invoke this method;</li>
     * <li>keep iterating on this object until it doesn't return any more rows;</li>
     * <li>retrieve the paging state with {@code getExecutionInfo().getPagingState()}, and reinject it in the statement
     * to resume the iteration.</li>
     * </ul>
     */
    void cancel();

    /**
     * Returns information on the execution of the query, and on the response frame corresponding to the current state
     * of the iteration.
     * <p/>
     * The driver does not support query traces for continuous queries, the corresponding field will always be
     * {@code null}.
     */
    ExecutionInfo getExecutionInfo();
}
