/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.CCMDseTestsSupport;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Uninterruptibles;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.datastax.driver.core.ContinuousPagingOptions.PageUnit.BYTES;
import static com.datastax.driver.core.ContinuousPagingOptions.PageUnit.ROWS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

@DseVersion(major = 5.1)
public class ContinuousPagingTest extends CCMDseTestsSupport {

    public static final String KEY = "k";

    @Override
    public void onTestContextInitialized() {
        execute("CREATE TABLE test (k text, v int, PRIMARY KEY (k, v))");
        for (int i = 0; i < 100; i++) {
            execute(String.format("INSERT INTO test (k, v) VALUES ('%s', %d)", KEY, i));
        }
    }

    private ContinuousPagingSession cSession() {
        return (ContinuousPagingSession) super.session();
    }

    @DataProvider
    Object[][] pagingOptions() {
        return new Object[][]{
                {ContinuousPagingOptions.builder().withPageSize(100, ROWS).build(), 100, 1}, // exact # of rows.
                {ContinuousPagingOptions.builder().withPageSize(99, ROWS).build(), 100, 2}, //# of rows - 1.
                {ContinuousPagingOptions.builder().withPageSize(50, ROWS).build(), 100, 2}, //# of rows / 2.
                {ContinuousPagingOptions.builder().withPageSize(1, ROWS).build(), 100, 100}, //# 1 row per page.
                {ContinuousPagingOptions.builder().withPageSize(10, ROWS).withMaxPages(10).build(), 100, 10}, // 10 rows per page, 10 pages overall = 100 (exact).
                {ContinuousPagingOptions.builder().withPageSize(10, ROWS).withMaxPages(9).build(), 90, 9}, // 10 rows per page, 9 pages overall = 90 (less than exact number of pages).
                {ContinuousPagingOptions.builder().withPageSize(10, ROWS).withMaxPages(0).withMaxPagesPerSecond(2).build(), 100, 10}, // 10 rows per page, 2 pages per second should take ~5secs.
                {ContinuousPagingOptions.builder().withPageSize(8, BYTES).build(), 100, 100}, // 8 bytes per page == 1 row per page as len(4) + int(4) for each row.
                {ContinuousPagingOptions.builder().withPageSize(16, BYTES).build(), 100, 50}, // 16 bytes per page == 2 row page per page.
                {ContinuousPagingOptions.builder().withPageSize(32, BYTES).build(), 100, 25} // 32 bytes per page == 4 row per page.
        };
    }

    /**
     * Validates {@link ContinuousPagingSession#executeContinuously(Statement, ContinuousPagingOptions)} with null
     * paging options throws an early NullPointerException.
     *
     * @test_category queries
     * @jira_ticket JAVA-1322
     * @since 1.2.0
     */
    @Test(groups = "short", expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Options must not be null")
    public void should_throw_exception_when_given_null_options_sync() {
        SimpleStatement statement = new SimpleStatement("SELECT v from test where k=?", KEY);
        cSession().executeContinuously(statement, null);
    }

    /**
     * Validates {@link ContinuousPagingSession#executeContinuouslyAsync(Statement, ContinuousPagingOptions)} with null
     * paging options throws an early NullPointerException.
     *
     * @test_category queries
     * @jira_ticket JAVA-1322
     * @since 1.2.0
     */
    @Test(groups = "short", expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Options must not be null")
    public void should_throw_exception_when_given_null_options_async() {
        SimpleStatement statement = new SimpleStatement("SELECT v from test where k=?", KEY);
        cSession().executeContinuouslyAsync(statement, null);
    }

    /**
     * Validates {@link ContinuousPagingSession#executeContinuously(Statement, ContinuousPagingOptions)} with a variety
     * of paging options and ensures in all cases the expected number of rows come back.
     *
     * @test_category queries
     * @jira_ticket JAVA-1322
     * @since 1.2.0
     */
    @Test(groups = "short", dataProvider = "pagingOptions")
    public void synchronous_paging_with_options(ContinuousPagingOptions options, int expectedRows, @SuppressWarnings("unused") int expectedPages) {
        SimpleStatement statement = new SimpleStatement("SELECT v from test where k=?", KEY);
        ContinuousPagingResult result = cSession().executeContinuously(statement, options);

        int i = 0;
        for (Row row : result) {
            assertThat(row.getInt("v")).isEqualTo(i);
            i++;
        }

        assertThat(i).isEqualTo(expectedRows);
    }

    /**
     * Validates that {@link ContinuousPagingResult#cancel()} will cancel a continuous paging session by setting
     * {@link ContinuousPagingOptions#maxPagesPerSecond} to 1 and sending a cancel immediately and ensuring the
     * total number of rows iterated over is equal to the size of {@link ContinuousPagingOptions#pageSize}.
     *
     * @test_category queries
     * @jira_ticket JAVA-1322
     * @since 1.2.0
     */
    @Test(groups = "short")
    public void should_cancel_with_synchronous_paging() throws Exception {
        SimpleStatement statement = new SimpleStatement("SELECT v from test where k=?", KEY);
        // create options and throttle at a page per second so cancel can go out before the next row is sent.
        // Note that this might not be perfect if there are pauses in the JVM and cancel isn't sent soon enough.
        ContinuousPagingOptions options = ContinuousPagingOptions.builder().withPageSize(10, ROWS).withMaxPages(0).withMaxPagesPerSecond(1).build();
        ContinuousPagingResult pagingResult = cSession().executeContinuously(statement, options);
        pagingResult.cancel();

        int i = 0;
        for (Row row : pagingResult) {
            assertThat(row.getInt("v")).isEqualTo(i);
            i++;
        }

        // Expect only 10 rows as paging was cancelled immediately.
        assertThat(i).isEqualTo(10);
    }

    /**
     * Validates {@link ContinuousPagingSession#executeContinuouslyAsync(Statement, ContinuousPagingOptions)} with a variety
     * of paging options and ensures in all cases the expected number of rows come back and the expected number of
     * pages are received.
     *
     * @test_category queries
     * @jira_ticket JAVA-1322
     * @since 1.2.0
     */
    @Test(groups = "short", dataProvider = "pagingOptions")
    public void asynchronous_paging_with_options(ContinuousPagingOptions options, int expectedRows, int expectedPages) throws Exception {
        SimpleStatement statement = new SimpleStatement("SELECT v from test where k=?", KEY);
        ListenableFuture<AsyncContinuousPagingResult> result = cSession().executeContinuouslyAsync(statement, options);

        ListenableFuture<PageStatistics> future = Futures.transform(result, new AsyncContinuousPagingFunction());

        PageStatistics stats = Uninterruptibles.getUninterruptibly(future, 30, TimeUnit.SECONDS);
        assertThat(stats.rows).isEqualTo(expectedRows);
        assertThat(stats.pages).isEqualTo(expectedPages);
    }

    /* TODO: There isn't a deterministic way here to determine that the cancel message was sent.
     * Consider rewriting a mock to ensure cancel is sent but for now this can be validated by observing the debug
     * logs when this test runs.
     */

    /**
     * Validates that {@link AsyncContinuousPagingResult#cancel()} will cancel a continuous paging session by setting
     * {@link ContinuousPagingOptions#maxPagesPerSecond} to 1 and sending a cancel after the first page is received and
     * then ensuring that the future returned from {@link AsyncContinuousPagingResult#nextPage()} fails.
     *
     * @test_category queries
     * @jira_ticket JAVA-1322
     * @since 1.2.0
     */
    @Test(groups = "short")
    public void should_cancel_with_asynchronous_paging() throws Exception {
        SimpleStatement statement = new SimpleStatement("SELECT v from test where k=?", KEY);
        // create options and throttle at a page per second so cancel can go out before the next row is sent.
        // Note that this might not be perfect if there are pauses in the JVM and cancel isn't sent soon enough.
        ContinuousPagingOptions options = ContinuousPagingOptions.builder().withPageSize(10, ROWS).withMaxPages(0).withMaxPagesPerSecond(1).build();

        ListenableFuture<AsyncContinuousPagingResult> future = cSession().executeContinuouslyAsync(statement, options);

        AsyncContinuousPagingResult pagingResult = Uninterruptibles.getUninterruptibly(future, 30, TimeUnit.SECONDS);
        // Calling cancel on the previous result should cause the next future to timeout.
        pagingResult.cancel();

        ListenableFuture<AsyncContinuousPagingResult> nextPageFuture = pagingResult.nextPage();

        try {
            // Expect future to fail since it was cancelled.
            Uninterruptibles.getUninterruptibly(nextPageFuture, 1, TimeUnit.SECONDS);
            fail("Expected an execution exception since paging was cancelled.");
        } catch (ExecutionException te) {
            // expected
            assertThat(te.getMessage().contains("was cancelled"));
        }
    }

    /**
     * Validates that {@link AsyncContinuousPagingResult#cancel()} will cancel a continuous paging session and current
     * tracked {@link ListenableFuture} tied to the paging session.
     *
     * @test_category queries
     * @jira_ticket JAVA-1322
     * @since 1.2.0
     */
    @Test(groups = "short")
    public void should_cancel_future_when_cancelling_previous_result() throws Exception {
        SimpleStatement statement = new SimpleStatement("SELECT v from test where k=?", KEY);
        // create options and throttle at a page per second so cancel can go out before the next row is sent.
        // Note that this might not be perfect if there are pauses in the JVM and cancel isn't sent soon enough.
        ContinuousPagingOptions options = ContinuousPagingOptions.builder().withPageSize(10, ROWS).withMaxPages(0).withMaxPagesPerSecond(1).build();

        ListenableFuture<AsyncContinuousPagingResult> future = cSession().executeContinuouslyAsync(statement, options);

        AsyncContinuousPagingResult pagingResult = Uninterruptibles.getUninterruptibly(future, 30, TimeUnit.SECONDS);

        ListenableFuture<AsyncContinuousPagingResult> nextPageFuture = pagingResult.nextPage();
        // Calling cancel on the previous result should cause the current future to be cancelled.
        pagingResult.cancel();
        assertThat(nextPageFuture.isCancelled()).isTrue();

        try {
            // Expect future to be cancelled since the previous result was cancelled.
            Uninterruptibles.getUninterruptibly(nextPageFuture, 1, TimeUnit.SECONDS);
            fail("Expected a cancellation exception since previous result was cancelled.");
        } catch (CancellationException ce) {
            // expected
        }
    }

    /**
     * Validates that {@link ListenableFuture#cancel(boolean)} will cancel a continuous paging session by setting
     * {@link ContinuousPagingOptions#maxPagesPerSecond} to 1 and sending a cancel after the first page is received and
     * then ensuring that the future returned from {@link AsyncContinuousPagingResult#nextPage()} is cancelled.
     *
     * @test_category queries
     * @jira_ticket JAVA-1322
     * @since 1.2.0
     */
    @Test(groups = "short")
    public void should_cancel_when_future_is_cancelled() throws Exception {
        SimpleStatement statement = new SimpleStatement("SELECT v from test where k=?", KEY);
        // create options and throttle at a page per second so cancel can go out before the next row is sent.
        // Note that this might not be perfect if there are pauses in the JVM and cancel isn't sent soon enough.
        ContinuousPagingOptions options = ContinuousPagingOptions.builder().withPageSize(10, ROWS).withMaxPages(0).withMaxPagesPerSecond(1).build();

        ListenableFuture<AsyncContinuousPagingResult> future = cSession().executeContinuouslyAsync(statement, options);

        AsyncContinuousPagingResult pagingResult = Uninterruptibles.getUninterruptibly(future, 30, TimeUnit.SECONDS);

        ListenableFuture<AsyncContinuousPagingResult> nextPageFuture = pagingResult.nextPage();

        nextPageFuture.cancel(false);
        assertThat(nextPageFuture.isCancelled()).isTrue();

        try {
            // Expect cancellation.
            Uninterruptibles.getUninterruptibly(nextPageFuture, 1, TimeUnit.SECONDS);
            fail("Expected a cancellation exception since fuure was cancelled.");
        } catch (CancellationException ce) {
            // expected
        }
    }

    private static class PageStatistics {
        int rows;
        int pages;

        PageStatistics(int rows, int pages) {
            this.rows = rows;
            this.pages = pages;
        }
    }

    /**
     * A function that when invoked, will return a transformed future with another {@link AsyncContinuousPagingFunction}
     * wrapping {@link AsyncContinuousPagingResult#nextPage()} if there are more pages, otherwise returns an
     * immediate future that shares {@link PageStatistics} about how many rows were returned and how many pages were
     * encountered.
     * <p>
     * Note that if observe that data is not parsed in order this future fails with an Exception.
     */
    private static class AsyncContinuousPagingFunction implements AsyncFunction<AsyncContinuousPagingResult, PageStatistics> {

        private final int rowsSoFar;

        AsyncContinuousPagingFunction() {
            this(0);
        }

        AsyncContinuousPagingFunction(int rowsSoFar) {
            this.rowsSoFar = rowsSoFar;
        }

        @Override
        public ListenableFuture<PageStatistics> apply(AsyncContinuousPagingResult input) throws Exception {
            int rows = rowsSoFar;
            // Iterate over page and ensure data is in order.
            for (Row row : input.currentPage()) {
                int v = row.getInt("v");
                if (v != rows) {
                    throw new Exception(String.format("Expected v == %d, got %d.", rows, v));
                }
                rows++;
            }

            // If on last page, complete future, otherwise keep iterating.
            if (input.isLast()) {
                // DSE may send an empty page as it can't always know if it's done paging or not yet.
                // See: CASSANDRA-8871.  In this case, don't count this page.
                int pages = rows == rowsSoFar ? input.pageNumber() - 1 : input.pageNumber();
                return Futures.immediateFuture(new PageStatistics(rows, pages));
            } else {
                return Futures.transform(input.nextPage(), new AsyncContinuousPagingFunction(rows));
            }
        }
    }
}
