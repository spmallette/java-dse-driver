/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.codahale.metrics.Timer;
import com.datastax.driver.core.Message.Request;
import com.datastax.driver.core.RequestHandler.QueryPlan;
import com.datastax.driver.core.RequestHandler.QueryState;
import com.datastax.driver.core.exceptions.*;
import com.datastax.driver.core.policies.RetryPolicy;
import com.datastax.driver.core.utils.MoreFutures;
import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Handles a request that supports multiple response messages.
 * <p>
 * This is similar to {@link RequestHandler}, but with the following differences:
 * <ul>
 * <li>the connection is not released after the first request. The caller must invoke {@link #release()} when it detects
 * that the request has finished executing on the server;</li>
 * <li>speculative executions are not supported.</li>
 * </ul>
 */
class MultiResponseRequestHandler implements Connection.ResponseCallback {
    private static final Logger logger = LoggerFactory.getLogger(MultiResponseRequestHandler.class);

    private final String id;
    final SessionManager manager;
    private final Callback callback;
    private final Message.Request initialRequest;
    final Statement statement;
    private final QueryPlan queryPlan;
    final int timeoutMillis;
    private final Timer.Context timerContext;
    private final AtomicReference<QueryState> queryStateRef;

    private volatile List<Host> triedHosts;
    private volatile ConcurrentMap<InetSocketAddress, Throwable> errors;
    private volatile Host current;
    private volatile Connection connection;
    private volatile Connection.ResponseHandler connectionHandler;
    private volatile ConsistencyLevel retryConsistencyLevel;
    // This represents the number of times a retry has been triggered by the RetryPolicy (this is different from
    // queryStateRef.get().retryCount, because some retries don't involve the policy, for example after an
    // UNPREPARED response).
    // This is incremented by one writer at a time, so volatile is good enough.
    private volatile int retriesByPolicy;
    private volatile ExecutionInfo info;
    private volatile boolean gotFirstResult;

    MultiResponseRequestHandler(SessionManager manager, Callback callback, Statement statement) {
        this.id = Long.toString(System.identityHashCode(this));
        if (logger.isTraceEnabled())
            logger.trace("[{}] {}", id, statement);
        this.manager = manager;
        this.callback = callback;
        this.initialRequest = callback.getRequest();
        this.statement = statement;
        this.queryPlan = new QueryPlan(manager.loadBalancingPolicy().newQueryPlan(manager.poolsState.keyspace, statement));
        this.timeoutMillis = statement.getReadTimeoutMillis() >= 0
                ? statement.getReadTimeoutMillis()
                : manager.configuration().getSocketOptions().getReadTimeoutMillis();

        this.timerContext = metricsEnabled()
                ? metrics().getRequestsTimer().time()
                : null;

        this.queryStateRef = new AtomicReference<QueryState>(QueryState.INITIAL);

        callback.register(this);
    }

    private boolean metricsEnabled() {
        return manager.configuration().getMetricsOptions().isEnabled();
    }

    private Metrics metrics() {
        return manager.cluster.manager.metrics;
    }

    void sendRequest() {
        try {
            Host host;
            while ((host = queryPlan.next()) != null && !queryStateRef.get().isCancelled()) {
                if (query(host))
                    return;
            }
            reportNoMoreHosts();
        } catch (Exception e) {
            // Shouldn't happen really, but if ever the loadbalancing policy returned iterator throws, we don't want to block.
            setException(null, new DriverInternalError("An unexpected error happened while sending requests", e), false);
        }
    }

    /**
     * Release the local resources associated with the request.
     * <p/>
     * This should be called only when the caller detects that the request has finished running server-side, such as
     * after the last page.
     * <p/>
     * To stop a query that is still running, use {@link #cancel()}.
     */
    void release() {
        release(connection);
        if (timerContext != null) {
            timerContext.stop();
        }
    }

    private boolean query(final Host host) {
        HostConnectionPool pool = manager.pools.get(host);
        if (pool == null || pool.isClosed())
            return false;

        if (logger.isTraceEnabled())
            logger.trace("[{}] Querying node {}", id, host);

        PoolingOptions poolingOptions = manager.configuration().getPoolingOptions();
        ListenableFuture<Connection> connectionFuture = pool.borrowConnection(
                poolingOptions.getPoolTimeoutMillis(), TimeUnit.MILLISECONDS, poolingOptions.getMaxQueueSize());
        Futures.addCallback(connectionFuture, new FutureCallback<Connection>() {
            @Override
            public void onSuccess(Connection connection) {
                MultiResponseRequestHandler.this.connection = connection;
                if (current != null) {
                    if (triedHosts == null) {
                        triedHosts = new CopyOnWriteArrayList<Host>();
                    }
                    triedHosts.add(current);
                }
                current = host;
                try {
                    write(connection, MultiResponseRequestHandler.this);
                } catch (ConnectionException e) {
                    // If we have any problem with the connection, move to the next node.
                    if (metricsEnabled())
                        metrics().getErrorMetrics().getConnectionErrors().inc();
                    if (connection != null)
                        connection.release();
                    logError(host.getSocketAddress(), e);
                    sendRequest();
                } catch (BusyConnectionException e) {
                    // The pool shouldn't have give us a busy connection unless we've maxed up the pool, so move on to the next host.
                    connection.release();
                    logError(host.getSocketAddress(), e);
                    sendRequest();
                } catch (RuntimeException e) {
                    if (connection != null)
                        connection.release();
                    logger.error("Unexpected error while querying " + host.getAddress(), e);
                    logError(host.getSocketAddress(), e);
                    sendRequest();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (t instanceof BusyPoolException) {
                    logError(host.getSocketAddress(), t);
                } else {
                    logger.error("Unexpected error while querying " + host.getAddress(), t);
                    logError(host.getSocketAddress(), t);
                }
                sendRequest();
            }
        });
        return true;
    }

    private void write(Connection connection, Connection.ResponseCallback responseCallback) throws ConnectionException, BusyConnectionException {
        // Make sure cancel() does not see a stale connectionHandler if it sees the new query state
        // before connection.write has completed
        connectionHandler = null;

        // Ensure query state is "in progress" (can be already if connection.write failed on a previous node and we're retrying)
        while (true) {
            QueryState previous = queryStateRef.get();
            if (previous.isCancelled()) {
                release(connection);
                return;
            }
            if (previous.inProgress || queryStateRef.compareAndSet(previous, previous.startNext()))
                break;
        }

        connectionHandler = connection.write(responseCallback, statement.getReadTimeoutMillis(), false, true);
        // Only start the timeout when we're sure connectionHandler is set. This avoids an edge case where onTimeout() was triggered
        // *before* the call to connection.write had returned.
        connectionHandler.startTimeout();
    }

    void cancel() {
        // Atomically set a special QueryState, that will cause any further operation to abort.
        // We want to remember whether a request was in progress when we did this, so there are two cancel states.
        while (true) {
            QueryState previous = queryStateRef.get();
            if (previous.isCancelled()) {
                return;
            } else if (previous == QueryState.INITIAL && queryStateRef.compareAndSet(previous, QueryState.CANCELLED_WHILE_COMPLETE)) {
                // Nothing was sent to the server yet, so nothing to do
                logger.trace("[{}] Cancelled before the first request was sent", id);
                return;
            } else if (previous.inProgress && queryStateRef.compareAndSet(previous, QueryState.CANCELLED_WHILE_IN_PROGRESS)) {
                logger.trace("[{}] Cancelled during the initial request", id);
                // Contrary to single-response requests (see RequestHandler), we don't remove the handler right away,
                // because the server could still send more than one response after the cancellation, causing
                // Connection.Dispatcher.channelRead0 to release the streamId too soon. We only remove the handler when
                // we receive the last response, or when the cancel request succeeds.
                sendCancelRequest();
                return;
            } else if (!previous.inProgress && queryStateRef.compareAndSet(previous, QueryState.CANCELLED_WHILE_COMPLETE)) {
                logger.trace("[{}] Cancelled after initial request complete", id);
                sendCancelRequest();
                return;
            }
        }
    }

    private void sendCancelRequest() {
        final Connection.ResponseCallback cancelResponseCallback = new Connection.ResponseCallback() {
            @Override
            public Request request() {
                return callback.getCancelRequest(connectionHandler.streamId);
            }

            @Override
            public void onSet(Connection connection, Message.Response response, long latency, int retryCount) {
                logger.trace("[{}] Cancelled successfully");
                connection.release(); // for the stream of the cancel request
                MultiResponseRequestHandler.this.release(); // for the stream of the continuous query
            }

            @Override
            public void onException(Connection connection, Exception exception, long latency, int retryCount) {
                logger.warn("[" + id + "] Cancel request failed. " +
                        "This is not critical (the request will eventually time out server-side).", exception);
                connection.release();
            }

            @Override
            public boolean onTimeout(Connection connection, long latency, int retryCount) {
                logger.warn("[{}] Cancel request timed out " +
                        "This is not critical (the request will eventually time out server-side).", id);
                connection.release();
                return false;
            }

            @Override
            public int retryCount() {
                return 0;
            }
        };
        ListenableFuture<Void> futureWrite = Futures.transform(
                // Borrow again, because the cancel request uses a different streamId
                manager.pools.get(current).borrowConnection(0, TimeUnit.MILLISECONDS, 0),
                new Function<Connection, Void>() {
                    @Override
                    public Void apply(Connection c) {
                        logger.trace("[{}] Sending cancel request" + id);
                        connection.write(cancelResponseCallback, timeoutMillis, true, false);
                        return null;
                    }
                });
        Futures.addCallback(futureWrite, new MoreFutures.FailureCallback<Void>() {
            @Override
            public void onFailure(Throwable t) {
                logger.warn("[" + id + "] Error writing cancel request. " +
                        "This is not critical (the request will eventually time out server-side).", t);
            }
        });
    }

    private void release(Connection connection) {
        if (connectionHandler != null) {
            connectionHandler.cancelHandler();
        }
        connection.release();
    }

    @Override
    public Request request() {
        if (retryConsistencyLevel != null && retryConsistencyLevel != initialRequest.consistency())
            return initialRequest.copy(retryConsistencyLevel);
        else
            return initialRequest;
    }

    @Override
    public void onSet(Connection connection, Message.Response response, long latency, int retryCount) {
        QueryState queryState = queryStateRef.get();
        if (!gotFirstResult) {
            if (!queryState.isInProgressAt(retryCount)
                    || !queryStateRef.compareAndSet(queryState, queryState.complete())) {
                logger.debug("onSet triggered but the response was completed by another thread, cancelling (retryCount = {}, queryState = {}, queryStateRef = {})",
                        retryCount, queryState, queryStateRef.get());
                return;
            }
        }

        Exception exceptionToReport;
        try {
            switch (response.type) {
                case RESULT:
                    setResult(connection, response);
                    break;
                case ERROR:
                    Responses.Error err = (Responses.Error) response;
                    exceptionToReport = err.asException(connection.address);
                    RetryPolicy.RetryDecision retry = null;
                    // Retries are only handled for the first response. For subsequent responses, errors will always
                    // be reported directly.
                    if (!gotFirstResult) {
                        RetryPolicy retryPolicy = retryPolicy();
                        switch (err.code) {
                            case READ_TIMEOUT:
                                release(connection);
                                assert err.infos instanceof ReadTimeoutException;
                                ReadTimeoutException rte = (ReadTimeoutException) err.infos;
                                retry = retryPolicy.onReadTimeout(statement,
                                        rte.getConsistencyLevel(),
                                        rte.getRequiredAcknowledgements(),
                                        rte.getReceivedAcknowledgements(),
                                        rte.wasDataRetrieved(),
                                        retriesByPolicy);
                                if (metricsEnabled()) {
                                    metrics().getErrorMetrics().getReadTimeouts().inc();
                                    if (retry.getType() == RetryPolicy.RetryDecision.Type.RETRY)
                                        metrics().getErrorMetrics().getRetriesOnReadTimeout().inc();
                                    if (retry.getType() == RetryPolicy.RetryDecision.Type.IGNORE)
                                        metrics().getErrorMetrics().getIgnoresOnReadTimeout().inc();
                                }
                                break;
                            case WRITE_TIMEOUT:
                                release(connection);
                                assert err.infos instanceof WriteTimeoutException;
                                WriteTimeoutException wte = (WriteTimeoutException) err.infos;
                                String msg = String.format("Unexpected error for %s, multi-response query are expected to be read-only", id);
                                logger.error(msg, wte);
                                setException(connection, new DriverInternalError(msg, wte), true);
                                break;
                            case UNAVAILABLE:
                                release(connection);
                                assert err.infos instanceof UnavailableException;
                                UnavailableException ue = (UnavailableException) err.infos;
                                retry = retryPolicy.onUnavailable(statement,
                                        ue.getConsistencyLevel(),
                                        ue.getRequiredReplicas(),
                                        ue.getAliveReplicas(),
                                        retriesByPolicy);
                                if (metricsEnabled()) {
                                    metrics().getErrorMetrics().getUnavailables().inc();
                                    if (retry.getType() == RetryPolicy.RetryDecision.Type.RETRY)
                                        metrics().getErrorMetrics().getRetriesOnUnavailable().inc();
                                    if (retry.getType() == RetryPolicy.RetryDecision.Type.IGNORE)
                                        metrics().getErrorMetrics().getIgnoresOnUnavailable().inc();
                                }
                                break;
                            case OVERLOADED:
                                release(connection);
                                assert exceptionToReport instanceof OverloadedException;
                                logger.warn("Host {} is overloaded.", connection.address);
                                retry = computeRetryDecisionOnRequestError((OverloadedException) exceptionToReport);
                                break;
                            case SERVER_ERROR:
                                release(connection);
                                assert exceptionToReport instanceof ServerError;
                                logger.warn("{} replied with server error ({}), defuncting connection.", connection.address, err.message);
                                // Defunct connection
                                connection.defunct(exceptionToReport);
                                retry = computeRetryDecisionOnRequestError((ServerError) exceptionToReport);
                                break;
                            case IS_BOOTSTRAPPING:
                                connection.release();
                                assert exceptionToReport instanceof BootstrappingException;
                                logger.error("Query sent to {} but it is bootstrapping. This shouldn't happen but trying next host.", connection.address);
                                if (metricsEnabled()) {
                                    metrics().getErrorMetrics().getOthers().inc();
                                }
                                logError(connection.address, exceptionToReport);
                                retry(false, null);
                                return;
                            case UNPREPARED:
                                // Do not release connection yet, because we might reuse it to send the PREPARE message (see write() call below)
                                assert err.infos instanceof MD5Digest;
                                MD5Digest id = (MD5Digest) err.infos;
                                PreparedStatement toPrepare = manager.cluster.manager.preparedQueries.get(id);
                                if (toPrepare == null) {
                                    // This shouldn't happen
                                    release(connection);
                                    msg = String.format("Tried to execute unknown prepared query %s", id);
                                    logger.error(msg);
                                    setException(connection, new DriverInternalError(msg), true);
                                    return;
                                }

                                String currentKeyspace = connection.keyspace();
                                String prepareKeyspace = toPrepare.getQueryKeyspace();
                                if (prepareKeyspace != null && (currentKeyspace == null || !currentKeyspace.equals(prepareKeyspace))) {
                                    // This shouldn't happen in normal use, because a user shouldn't try to execute
                                    // a prepared statement with the wrong keyspace set.
                                    // Fail fast (we can't change the keyspace to reprepare, because we're using a pooled connection
                                    // that's shared with other requests).
                                    release(connection);
                                    throw new IllegalStateException(String.format("Statement was prepared on keyspace %s, can't execute it on %s (%s)",
                                            toPrepare.getQueryKeyspace(), connection.keyspace(), toPrepare.getQueryString()));
                                }

                                logger.info("Query {} is not prepared on {}, preparing before retrying executing. "
                                                + "Seeing this message a few times is fine, but seeing it a lot may be source of performance problems",
                                        toPrepare.getQueryString(), connection.address);

                                write(connection, prepareAndRetry(toPrepare.getQueryString()));
                                // we're done for now, the prepareAndRetry callback will handle the rest
                                return;
                            default:
                                release(connection);
                                if (metricsEnabled())
                                    metrics().getErrorMetrics().getOthers().inc();
                                break;
                        }
                    }

                    if (retry == null)
                        setResult(connection, response);
                    else {
                        processRetryDecision(retry, connection, exceptionToReport, true);
                    }
                    break;
                default:
                    connection.release();
                    setResult(connection, response);
                    break;
            }
        } catch (Exception e) {
            setException(connection, e, false);
        }
    }

    private RetryPolicy retryPolicy() {
        return statement.getRetryPolicy() == null
                ? manager.configuration().getPolicies().getRetryPolicy()
                : statement.getRetryPolicy();
    }

    private RetryPolicy.RetryDecision computeRetryDecisionOnRequestError(DriverException exception) {
        RetryPolicy.RetryDecision decision;
        if (statement.isIdempotentWithDefault(manager.cluster.getConfiguration().getQueryOptions())) {
            decision = retryPolicy().onRequestError(statement, request().consistency(), exception, retriesByPolicy);
        } else {
            decision = RetryPolicy.RetryDecision.rethrow();
        }
        if (metricsEnabled()) {
            if (exception instanceof OperationTimedOutException) {
                metrics().getErrorMetrics().getClientTimeouts().inc();
                if (decision.getType() == RetryPolicy.RetryDecision.Type.RETRY)
                    metrics().getErrorMetrics().getRetriesOnClientTimeout().inc();
                if (decision.getType() == RetryPolicy.RetryDecision.Type.IGNORE)
                    metrics().getErrorMetrics().getIgnoresOnClientTimeout().inc();
            } else if (exception instanceof ConnectionException) {
                metrics().getErrorMetrics().getConnectionErrors().inc();
                if (decision.getType() == RetryPolicy.RetryDecision.Type.RETRY)
                    metrics().getErrorMetrics().getRetriesOnConnectionError().inc();
                if (decision.getType() == RetryPolicy.RetryDecision.Type.IGNORE)
                    metrics().getErrorMetrics().getIgnoresOnConnectionError().inc();
            } else {
                metrics().getErrorMetrics().getOthers().inc();
                if (decision.getType() == RetryPolicy.RetryDecision.Type.RETRY)
                    metrics().getErrorMetrics().getRetriesOnOtherErrors().inc();
                if (decision.getType() == RetryPolicy.RetryDecision.Type.IGNORE)
                    metrics().getErrorMetrics().getIgnoresOnOtherErrors().inc();
            }
        }
        return decision;
    }

    private void processRetryDecision(RetryPolicy.RetryDecision retryDecision, Connection connection, Exception exceptionToReport, boolean fromServer) {
        switch (retryDecision.getType()) {
            case RETRY:
                retriesByPolicy++;
                if (logger.isDebugEnabled())
                    logger.debug("[{}] Doing retry {} for query {} at consistency {}", id, retriesByPolicy, statement, retryDecision.getRetryConsistencyLevel());
                if (metricsEnabled())
                    metrics().getErrorMetrics().getRetries().inc();
                // log error for the current host if we are switching to another one
                if (!retryDecision.isRetryCurrent())
                    logError(connection.address, exceptionToReport);
                retry(retryDecision.isRetryCurrent(), retryDecision.getRetryConsistencyLevel());
                break;
            case RETHROW:
                setException(connection, exceptionToReport, fromServer);
                break;
            case IGNORE:
                if (metricsEnabled())
                    metrics().getErrorMetrics().getIgnores().inc();
                setResult(connection, new Responses.Result.Void());
                break;
        }
    }

    private void retry(final boolean retryCurrent, ConsistencyLevel newConsistencyLevel) {
        final Host h = current;
        if (newConsistencyLevel != null)
            this.retryConsistencyLevel = newConsistencyLevel;

        if (queryStateRef.get().isCancelled())
            return;

        if (!retryCurrent || !query(h))
            sendRequest();
    }

    private Connection.ResponseCallback prepareAndRetry(final String toPrepare) {
        return new Connection.ResponseCallback() {

            @Override
            public Message.Request request() {
                Requests.Prepare request = new Requests.Prepare(toPrepare);
                // propagate the original custom payload in the prepare request
                request.setCustomPayload(statement.getOutgoingPayload());
                return request;
            }

            @Override
            public int retryCount() {
                return MultiResponseRequestHandler.this.retryCount();
            }

            @Override
            public void onSet(Connection connection, Message.Response response, long latency, int retryCount) {
                QueryState queryState = queryStateRef.get();
                if (!queryState.isInProgressAt(retryCount) ||
                        !queryStateRef.compareAndSet(queryState, queryState.complete())) {
                    logger.debug("onSet triggered but the response was completed by another thread, cancelling (retryCount = {}, queryState = {}, queryStateRef = {})",
                            retryCount, queryState, queryStateRef.get());
                    return;
                }

                connection.release();

                switch (response.type) {
                    case RESULT:
                        if (((Responses.Result) response).kind == Responses.Result.Kind.PREPARED) {
                            logger.debug("Scheduling retry now that query is prepared");
                            retry(true, null);
                        } else {
                            logError(connection.address, new DriverException("Got unexpected response to prepare message: " + response));
                            retry(false, null);
                        }
                        break;
                    case ERROR:
                        logError(connection.address, new DriverException("Error preparing query, got " + response));
                        if (metricsEnabled())
                            metrics().getErrorMetrics().getOthers().inc();
                        retry(false, null);
                        break;
                    default:
                        // Something's wrong, so we return but we let setResult propagate the exception
                        setResult(connection, response);
                        break;
                }
            }

            @Override
            public void onException(Connection connection, Exception exception, long latency, int retryCount) {
                MultiResponseRequestHandler.this.onException(connection, exception, latency, retryCount);
            }

            @Override
            public boolean onTimeout(Connection connection, long latency, int retryCount) {
                QueryState queryState = queryStateRef.get();
                if (!queryState.isInProgressAt(retryCount) ||
                        !queryStateRef.compareAndSet(queryState, queryState.complete())) {
                    logger.debug("onTimeout triggered but the response was completed by another thread, cancelling (retryCount = {}, queryState = {}, queryStateRef = {})",
                            retryCount, queryState, queryStateRef.get());
                    return false;
                }
                connection.release();
                logError(connection.address, new OperationTimedOutException(connection.address, "Timed out waiting for response to PREPARE message"));
                retry(false, null);
                return true;
            }
        };
    }

    @Override
    public void onException(Connection connection, Exception exception, long latency, int retryCount) {
        QueryState queryState = queryStateRef.get();
        if (!gotFirstResult && (!queryState.isInProgressAt(retryCount) ||
                !queryStateRef.compareAndSet(queryState, queryState.complete()))) {
            logger.debug("onException triggered but the response was completed by another thread, cancelling (retryCount = {}, queryState = {}, queryStateRef = {})",
                    retryCount, queryState, queryStateRef.get());
            return;
        }

        try {
            release(connection);

            if (!gotFirstResult && (exception instanceof ConnectionException)) {
                RetryPolicy.RetryDecision decision = computeRetryDecisionOnRequestError((ConnectionException) exception);
                processRetryDecision(decision, connection, exception,
                        // In practice, onException is never called in response to a server error:
                        false);
            } else {
                setException(connection, exception, false);
            }
        } catch (Exception e) {
            // This shouldn't happen, but if it does, we want to signal the callback, not let it hang indefinitely
            setException(connection, new DriverInternalError("An unexpected error happened while handling exception " + exception, e), false);
        }
    }

    @Override
    public boolean onTimeout(Connection connection, long latency, int retryCount) {
        // timeout can only happen for the first page so don't check gotFirstResult
        QueryState queryState = queryStateRef.get();
        if (!queryState.isInProgressAt(retryCount)
                || !queryStateRef.compareAndSet(queryState, queryState.complete())) {
            logger.debug("onTimeout triggered but the response was completed by another thread, cancelling (retryCount = {}, queryState = {}, queryStateRef = {})",
                    retryCount, queryState, queryStateRef.get());
            return false;
        }

        try {
            // Never release the connection/handler on timeouts: the server might still send multiple responses after
            // the timeout. If we cancel the handler now, Connection.Dispatcher.channelRead0 will release the streamId
            // on the next response, and subsequent responses could corrupt other queries if the streamId is reused.

            OperationTimedOutException timeoutException = new OperationTimedOutException(connection.address, "Timed out waiting for server response");
            RetryPolicy.RetryDecision decision = computeRetryDecisionOnRequestError(timeoutException);
            processRetryDecision(decision, connection, timeoutException, false);
        } catch (Exception e) {
            // This shouldn't happen, but if it does, we want to signal the callback, not let it hang indefinitely
            setException(connection, new DriverInternalError("An unexpected error happened while handling timeout", e), false);
        }
        return true;
    }

    @Override
    public int retryCount() {
        return queryStateRef.get().retryCount;
    }

    private void setResult(Connection connection, Message.Response response) {
        gotFirstResult = true;
        logger.trace("[{}] Setting result", id);
        try {
            // Execution info describes the initial request, we will return the same object for all responses so cache
            // it
            if (info == null) {
                info = current.defaultExecutionInfo;
                if (triedHosts != null) {
                    triedHosts.add(current);
                    info = new ExecutionInfo(triedHosts);
                }
                if (retryConsistencyLevel != null)
                    info = info.withAchievedConsistency(retryConsistencyLevel);
                if (response.getCustomPayload() != null)
                    info = info.withIncomingPayload(response.getCustomPayload());
            }
            callback.onResponse(connection, response, info, statement);
        } catch (Exception e) {
            callback.onException(connection, new DriverInternalError(
                    "Unexpected exception while setting final result from " + response, e), false);
        }
    }

    private void setException(Connection connection, Exception exception, boolean fromServer) {
        logger.trace("[{}] Setting exception", id);
        callback.onException(connection, exception, fromServer);
    }

    private void logError(InetSocketAddress address, Throwable exception) {
        logger.debug("[{}] Error querying {} : {}", id, address, exception.toString());
        if (errors == null) {
            synchronized (this) {
                if (errors == null) {
                    errors = new ConcurrentHashMap<InetSocketAddress, Throwable>();
                }
            }
        }
        errors.put(address, exception);
    }

    private void reportNoMoreHosts() {
        setException(null, new NoHostAvailableException(
                errors == null ? Collections.<InetSocketAddress, Throwable>emptyMap() : errors), false);
    }

    interface Callback {
        void register(MultiResponseRequestHandler handler);

        Request getRequest();

        Request getCancelRequest(int streamId);

        void onResponse(Connection connection, Message.Response response, ExecutionInfo info, Statement statement);

        void onException(Connection connection, Exception exception, boolean fromServer);
    }
}
