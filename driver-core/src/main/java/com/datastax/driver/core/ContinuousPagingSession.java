/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * A session that has the ability to execute continuous paging queries.
 * <p/>
 * This interface exists solely for backward compatibility purposes, because adding the methods directly to
 * {@code Session} would have broken clients that implemented it. In practice, any session returned by the driver is
 * also a continuous paging session, and can be safely casted.
 * <p/>
 * This will be merged in the parent interface in the next major version.
 */
public interface ContinuousPagingSession extends Session {
    /**
     * Executes the provided query with continuous paging.
     * <p/>
     * The server will push all requested pages asynchronously, according to the options passed in as a parameter. The
     * client should consume all pages as quickly as possible, to avoid blocking the server for too long. The server
     * will adjust the rate according to the client speed, but it will give up if the client does not consume any pages
     * in a period of time equal to the read request timeout.
     * <p/>
     * This functionality is typically used by tools that want to retrieve the entire data set as quickly as possible,
     * typically for analytics queries. Such tools should also make sure to contact a replica directly, by setting a
     * routing key or token in the statement, so that the server can further optimize {@link ConsistencyLevel#ONE}
     * queries by reading data from local disk and keeping iterators open across pages. This optimization is only
     * available if the coordinator is a replica and the consistency level is {@code ONE}. If this is not the case, the
     * coordinator will retrieve pages one by one from replicas. Note that when the optimization kicks in (range query
     * at {@code ONE} performed directly on a replica), the snitch is bypassed and the coordinator will always chose
     * itself as a replica. Therefore, other functionality such as probabilistic read repair and speculative retry is
     * also not available when contacting a replica at {@code ONE}.
     *
     * @param statement the CQL query to execute (that can be any {@code Statement}).
     * @param options   the query options (this can't be null).
     * @return a future to the first asynchronous result.
     */
    ListenableFuture<AsyncContinuousPagingResult> executeContinuouslyAsync(final Statement statement,
                                                                           final ContinuousPagingOptions options);

    /**
     * Convenience method to execute a continuous query in a synchronous context.
     * <p/>
     * This method calls {@link #executeContinuouslyAsync(Statement, ContinuousPagingOptions)} internally, and takes
     * care of chaining the successive results into a convenient iterable, provided that you always access the result
     * from the same thread.
     *
     * @param statement the CQL query to execute (that can be any {@code Statement}).
     * @param options   the query options (this can't be null).
     * @return a synchronous iterable on the results.
     */
    ContinuousPagingResult executeContinuously(final Statement statement, final ContinuousPagingOptions options);
}
