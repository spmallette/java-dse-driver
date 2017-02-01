/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.datastax.driver.core.Message.Request;
import com.datastax.driver.core.Message.Response;
import com.datastax.driver.core.Requests.Cancel;
import com.datastax.driver.core.Responses.Result;
import com.datastax.driver.core.Responses.Result.Rows;
import com.datastax.driver.core.exceptions.DriverInternalError;
import com.datastax.driver.core.exceptions.OperationTimedOutException;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.channel.EventLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static com.datastax.driver.core.Message.Response.Type.ERROR;
import static com.datastax.driver.core.Message.Response.Type.RESULT;
import static com.datastax.driver.core.Responses.Result.Kind.ROWS;

/**
 * Buffers the stream of responses to a continuous query between the network handler and the client.
 */
class ContinuousPagingQueue implements MultiResponseRequestHandler.Callback {
    private static final Logger logger = LoggerFactory.getLogger(ContinuousPagingQueue.class);

    // How many responses we accumulate before applying backpressure.
    // Note that because this is done asynchronously, the queue might actually grow bigger than that.
    private static final int MAX_ENQUEUED_RESPONSES = 4;

    private final Request request;

    // Coordinates access to shared state. This is acquired from the I/O thread, but in practice there is little
    // contention.
    private final ReentrantLock lock;
    // Responses that we have received and have not been consumed by the client yet.
    // Only accessed while holding the lock.
    private final Queue<Object> queue;
    // If the client requested a page while the queue was empty, then it's waiting on that future.
    // Only accessed while holding the lock.
    private SettableFuture<AsyncContinuousPagingResult> pendingResult;

    private volatile MultiResponseRequestHandler handler;
    // How long the client waits between each page
    private volatile long timeoutMillis;

    // An integer that represents the state of the continuous paging request:
    // - if positive, it is the sequence number of the next expected page;
    // - if negative, it is a terminal state, identified by the constants below.
    // This is only mutated from the connection's event loop, so no synchronization is needed.
    private volatile int state;
    private static final int STATE_FINISHED = -1;
    private static final int STATE_FAILED = -2;

    // These are set by the first response, and are constant for the rest of the execution
    private volatile Connection connection;
    private volatile ColumnDefinitions columnDefinitions;

    ContinuousPagingQueue(Request request,
                          SettableFuture<AsyncContinuousPagingResult> firstResult) {
        this.request = request;

        this.lock = new ReentrantLock();
        this.pendingResult = firstResult;
        this.queue = new ConcurrentLinkedQueue<Object>();

        this.state = 1;
    }

    @Override
    public void register(MultiResponseRequestHandler handler) {
        this.handler = handler;

        // Same timeout as the initial request
        this.timeoutMillis = handler.timeoutMillis;
    }

    @Override
    public Request getRequest() {
        return request;
    }

    @Override
    public Request getCancelRequest(int streamId) {
        return Cancel.continuousPaging(streamId);
    }

    @Override
    public void onResponse(Connection connection, Response response, ExecutionInfo info, Statement statement) {
        assert connection.channel.eventLoop().inEventLoop();
        if (state < 0) {
            logger.debug("Discarding {} response because the request has already completed", response.type);
            return;
        }
        this.connection = connection;
        if (response.type == RESULT && ((Result) response).kind == ROWS) {
            Rows rows = (Rows) response;
            if (rows.metadata.continuousPage.seqNo != state) {
                fail(new DriverInternalError(String.format("Received page number %d but was expecting %d",
                        rows.metadata.continuousPage.seqNo, state)), false);
            } else {
                if (rows.metadata.continuousPage.last) {
                    logger.debug("Received last page ({})", rows.metadata.continuousPage.seqNo);
                    state = STATE_FINISHED;
                    handler.release();
                } else {
                    logger.debug("Received page {}", rows.metadata.continuousPage.seqNo);
                    state = state + 1;
                }
                enqueueOrCompletePending(newResult(rows, info));
            }
        } else if (response.type == ERROR) {
            fail(((Responses.Error) response).asException(connection.address), true);
        } else {
            fail(new DriverInternalError("Unexpected response " + response.type), false);
        }
    }

    @Override
    public void onException(final Connection connection, final Exception exception, final boolean fromServer) {
        if (connection == null) {
            // This only happens when sending the initial request, if no host was available or if the iterator returned
            // by the LBP threw an exception. In either case the write was not even attempted, so we're sure we're not
            // going to race with responses or timeouts and we can complete without checking the state.
            logger.debug("Fail {} ({})", exception.getClass().getSimpleName(), exception.getMessage());
            enqueueOrCompletePending(exception);
        } else {
            EventLoop eventLoop = connection.channel.eventLoop();
            if (!eventLoop.inEventLoop()) {
                // reschedule so that the state is accessed from the right thread
                eventLoop.execute(new Runnable() {
                    @Override
                    public void run() {
                        onException(connection, exception, fromServer);
                    }
                });
            } else if (state > 0) {
                fail(exception, fromServer);
            }
        }
    }

    private void fail(Exception exception, boolean fromServer) {
        logger.debug("Got failure {} ({})", exception.getClass().getSimpleName(), exception.getMessage());
        if (fromServer) {
            // We can safely assume the server won't send any more responses, so release the streamId
            state = STATE_FAILED;
            handler.release();
            if (connection != null) {
                // Make sure we don't leave it stuck
                connection.channel.config().setAutoRead(true);
            }
        } else {
            // Cancel in case the error was purely client-side (server might still be executing the query)
            cancel();
        }
        enqueueOrCompletePending(exception);
    }

    // Enqueue a response or, if the client was already waiting for it, complete the pending future.
    private void enqueueOrCompletePending(Object pageOrError) {
        lock.lock();
        try {
            if (pendingResult != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Client was waiting on empty queue, completing with {}", asDebugString(pageOrError));
                }
                SettableFuture<AsyncContinuousPagingResult> tmp = pendingResult;
                pendingResult = null;
                complete(tmp, pageOrError);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Enqueuing {}", asDebugString(pageOrError));
                }
                enqueue(pageOrError);
            }
        } finally {
            lock.unlock();
        }
    }

    // Dequeue a response or, if the queue is empty, create the future that will get notified of the next response.
    ListenableFuture<AsyncContinuousPagingResult> dequeueOrCreatePending() {
        lock.lock();
        try {
            // Precondition: the client will not call this method until the previous call has completed (this is guaranteed
            // by our public API because in order to ask for the next page, you need the reference to the previous page --
            // see AsyncContinuousPagingResult#nextPage())
            assert pendingResult == null;

            Object head = dequeue();
            if (head != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Client queries on non-empty queue, returning immediate future of {}",
                            asDebugString(head));
                }
                return immediateFuture(head);
            } else if (state == STATE_FAILED) {
                logger.debug("Client queries on failed empty queue, returning failed future");
                return immediateFuture(new IllegalStateException(
                        "Can't get more results because the continuous query has failed already. " +
                                "Most likely this is because the query was cancelled"));
            } else {
                logger.debug("Client queries on empty queue, installing future");
                final SettableFuture<AsyncContinuousPagingResult> future = SettableFuture.create();
                future.addListener(new Runnable() {
                    @Override
                    public void run() {
                        if (future.isCancelled()) {
                            ContinuousPagingQueue.this.cancel();
                        }
                    }
                }, MoreExecutors.sameThreadExecutor());
                pendingResult = future;
                startTimeout();
                return future;
            }
        } finally {
            lock.unlock();
        }
    }

    private void enqueue(Object pageOrError) {
        assert lock.isHeldByCurrentThread();
        queue.add(pageOrError);
        // Backpressure: if the queue grows too large, disable auto-read so that the channel eventually becomes
        // non-writable on the server side (causing it to back off for a while)
        if (queue.size() == MAX_ENQUEUED_RESPONSES) {
            if (logger.isDebugEnabled()) {
                logger.debug("Exceeded {} queued response pages, disabling auto-read", queue.size());
            }
            connection.channel.config().setAutoRead(false);
        }
    }

    private Object dequeue() {
        assert lock.isHeldByCurrentThread();
        Object head = queue.poll();
        if (head != null && queue.size() == MAX_ENQUEUED_RESPONSES - 1) {
            if (logger.isDebugEnabled()) {
                logger.debug("Back to {} queued response pages, re-enabling auto-read", queue.size());
            }
            connection.channel.config().setAutoRead(true);
        }
        return head;
    }

    private void complete(SettableFuture<AsyncContinuousPagingResult> future, Object pageOrError) {
        if (pageOrError instanceof AsyncContinuousPagingResult) {
            future.set((AsyncContinuousPagingResult) pageOrError);
        } else {
            future.setException((Throwable) pageOrError);
        }
    }

    private ListenableFuture<AsyncContinuousPagingResult> immediateFuture(Object pageOrError) {
        return (pageOrError instanceof AsyncContinuousPagingResult)
                ? Futures.immediateFuture((AsyncContinuousPagingResult) pageOrError)
                : Futures.<AsyncContinuousPagingResult>immediateFailedFuture((Throwable) pageOrError);
    }

    private AsyncContinuousPagingResult newResult(Rows rows, ExecutionInfo info) {
        Statement statement = handler.statement;
        if (columnDefinitions == null) {
            if (rows.metadata.columns == null) {
                if (statement instanceof StatementWrapper) {
                    statement = ((StatementWrapper) statement).getWrappedStatement();
                }
                assert statement instanceof BoundStatement;
                columnDefinitions = ((BoundStatement) statement).statement.getPreparedId().resultSetMetadata;
                assert columnDefinitions != null;
            } else {
                columnDefinitions = rows.metadata.columns;
            }
        }

        Token.Factory tokenFactory = handler.manager.cluster.getMetadata().tokenFactory();
        ProtocolVersion protocolVersion = handler.manager.cluster.manager.protocolVersion();
        CodecRegistry codecRegistry = handler.manager.configuration().getCodecRegistry();

        info = info.with(null, // Don't handle query trace, it's unlikely to be used with continuous paging
                rows.warnings, rows.metadata.pagingState, statement, protocolVersion, codecRegistry);

        return new DefaultAsyncContinuousPagingResult(rows.data, columnDefinitions,
                rows.metadata.continuousPage.seqNo, rows.metadata.continuousPage.last, info,
                tokenFactory, protocolVersion, this);
    }

    void cancel() {
        if (state >= 0) {
            state = STATE_FAILED;
            handler.cancel();
            cancelPendingResult(); // if another thread is waiting on an empty queue, unblock it
            if (connection != null) {
                // Make sure we don't leave it stuck
                connection.channel.config().setAutoRead(true);
            }
        }
    }

    private void cancelPendingResult() {
        lock.lock();
        try {
            if (pendingResult != null) {
                pendingResult.cancel(true);
            }
        } finally {
            lock.unlock();
        }
    }

    private void startTimeout() {
        // We don't set a timeout for the initial query (because MultiResponseRequestHandler handles it). We set
        // connection on the initial response so it will be set.
        assert connection != null;
        final int expectedPage = state;
        if (expectedPage < 0) {
            return;
        }
        assert expectedPage > 1 : expectedPage;
        connection.channel.eventLoop().schedule(new Runnable() {
            @Override
            public void run() {
                if (state == expectedPage) {
                    fail(new OperationTimedOutException(connection.address,
                            String.format("Timed out waiting for page %d", expectedPage)), false);
                } else {
                    // Ignore if the request has moved on. This is simpler than trying to cancel the timeout.
                    logger.trace("Timeout fired for page {} but query already at state {}, skipping",
                            expectedPage, state);
                }
            }
        }, timeoutMillis, TimeUnit.MILLISECONDS);
    }

    private String asDebugString(Object pageOrError) {
        return (pageOrError instanceof AsyncContinuousPagingResult)
                ? "page " + ((AsyncContinuousPagingResult) pageOrError).pageNumber()
                : ((Exception) pageOrError).getClass().getSimpleName();
    }
}
