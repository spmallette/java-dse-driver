/*
 *      Copyright (C) 2012-2015 DataStax Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.datastax.driver.graph;

import com.datastax.driver.core.ExecutionInfo;
import com.datastax.driver.core.ResultSet;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Iterator;
import java.util.List;

import static com.datastax.driver.graph.GraphJsonUtils.ROW_TO_GRAPH_RESULT;

/**
 * The result set containing the Graph results returned from a query.
 */
public class GraphResultSet implements Iterable<GraphResult> {

    private final ResultSet wrapped;

    /**
     * This constructor is intended for internal use only, users should normally obtain
     * instances from {@link com.datastax.driver.DseSession#executeGraph(GraphStatement)}.
     */
    public GraphResultSet(ResultSet wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * Returns whether this GraphResultSet has more results.
     *
     * @return whether this GraphResultSet has more results.
     */
    public boolean isExhausted() {
        return wrapped.isExhausted();
    }

    /**
     * Returns the next result from this GraphResultSet.
     *
     * @return the next GraphResult in this GraphResultSet or null if this GraphResultSet is
     * exhausted.
     */
    public GraphResult one() {
        return ROW_TO_GRAPH_RESULT.apply(wrapped.one());
    }

    /**
     * Returns all the remaining GraphResults in this GraphResultSet as a list.
     * <p/>
     * Note that, contrary to {@code iterator()} or successive calls to
     * {@code one()}, this method forces fetching the full content of the GraphResultSet
     * at once, holding it all in memory in particular. It is thus recommended
     * to prefer iterations through {@code iterator()} when possible, especially
     * if the GraphResultSet can be big.
     *
     * @return a list containing the remaining results of this GraphResultSet. The
     * returned list is empty if and only the GraphResultSet is exhausted. The GraphResultSet
     * will be exhausted after a call to this method.
     */
    public List<GraphResult> all() {
        return Lists.transform(wrapped.all(), ROW_TO_GRAPH_RESULT);
    }

    /**
     * Returns an iterator over the GraphResults contained in this GraphResultSet.
     * <p/>
     * The {@link Iterator#next} method is equivalent to calling {@link #one}.
     * So this iterator will consume results from this GraphResultSet and after a
     * full iteration, the GraphResultSet will be empty.
     * <p/>
     * The returned iterator does not support the {@link Iterator#remove} method.
     *
     * @return an iterator that will consume and return the remaining GraphResults of
     * this GraphResultSet.
     */
    public Iterator<GraphResult> iterator() {
        return new Iterator<GraphResult>() {
            @Override
            public boolean hasNext() {
                return !wrapped.isExhausted();
            }

            @Override
            public GraphResult next() {
                return one();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * The number of GraphResults that can be retrieved from this result set without
     * blocking to fetch.
     *
     * @return the number of GraphResults readily available in this result set. If
     * {@link #isFullyFetched()}, this is the total number of GraphResults remaining
     * in this result set (after which the result set will be exhausted).
     */
    public int getAvailableWithoutFetching() {
        return wrapped.getAvailableWithoutFetching();
    }

    /**
     * Whether all results from this result set have been fetched from the
     * database.
     * <p/>
     * Note that if {@code isFullyFetched()}, then {@link #getAvailableWithoutFetching}
     * will return how many GraphResults remain in the result set before exhaustion. But
     * please note that {@code !isFullyFetched()} never guarantees that the result set
     * is not exhausted (you should call {@code isExhausted()} to verify it).
     *
     * @return whether all results have been fetched.
     */
    public boolean isFullyFetched() {
        return wrapped.isFullyFetched();
    }

    /**
     * Force fetching the next page of results for this result set, if any.
     * <p/>
     * This method is entirely optional. It will be called automatically while
     * the result set is consumed (through {@link #one}, {@link #all} or iteration)
     * when needed (i.e. when {@code getAvailableWithoutFetching() == 0} and
     * {@code isFullyFetched() == false}).
     * <p/>
     * You can however call this method manually to force the fetching of the
     * next page of results. This can allow to prefetch results before they are
     * strictly needed. For instance, if you want to prefetch the next page of
     * results as soon as there is less than 100 rows readily available in this
     * result set, you can do:
     * <pre>
     *   GraphResultSet rs = session.executeGraph(...);
     *   Iterator&lt;GraphResult&gt; iter = rs.iterator();
     *   while (iter.hasNext()) {
     *       if (rs.getAvailableWithoutFetching() == 100 &amp;&amp; !rs.isFullyFetched())
     *           rs.fetchMoreResults();
     *       GraphResult result = iter.next()
     *       ... process the result ...
     *   }
     * </pre>
     * This method is not blocking, so in the example above, the call to {@code
     * fetchMoreResults} will not block the processing of the 100 currently available
     * rows (but {@code iter.hasNext()} will block once those rows have been processed
     * until the fetch query returns, if it hasn't yet).
     * <p/>
     * Only one page of results (for a given result set) can be
     * fetched at any given time. If this method is called twice and the query
     * triggered by the first call has not returned yet when the second one is
     * performed, then the 2nd call will simply return a future on the currently
     * in progress query.
     *
     * @return a future on the completion of fetching the next page of results.
     * If the result set is already fully retrieved ({@code isFullyFetched() == true}),
     * then the returned future will return immediately but not particular error will be
     * thrown (you should thus call {@code isFullyFetched() to know if calling this
     * method can be of any use}).
     * @see ResultSet#fetchMoreResults()
     */
    public ListenableFuture<GraphResultSet> fetchMoreResults() {
        return Futures.transform(wrapped.fetchMoreResults(), new Function<ResultSet, GraphResultSet>() {
            @Override
            public GraphResultSet apply(ResultSet input) {
                return new GraphResultSet(input);
            }
        });
    }

    /**
     * Returns information on the execution of the last query made for this GraphResultSet.
     * <p/>
     * Note that in most cases, a GraphResultSet is fetched with only one query, but large
     * result sets can be paged and thus be retrieved by multiple queries. In that
     * case this method return the {@code ExecutionInfo} for the last query
     * performed. To retrieve the information for all queries, use {@link #getAllExecutionInfo}.
     * <p/>
     * The returned object includes basic information such as the queried hosts,
     * but also the Cassandra query trace if tracing was enabled for the query.
     *
     * @return the execution info for the last query made for this GraphResultSet.
     */
    public ExecutionInfo getExecutionInfo() {
        return wrapped.getExecutionInfo();
    }

    /**
     * Return the execution information for all queries made to retrieve this
     * GraphResultSet.
     * <p/>
     * Unless the GraphResultSet is large enough to get paged underneath, the returned
     * list will be singleton. If paging has been used however, the returned list
     * contains the {@code ExecutionInfo} for all the queries done to obtain this
     * GraphResultSet (at the time of the call) in the order those queries were made.
     *
     * @return a list of the execution info for all the queries made for this GraphResultSet.
     */
    public List<ExecutionInfo> getAllExecutionInfo() {
        return wrapped.getAllExecutionInfo();
    }

    /**
     * If the query that produced this GraphResultSet was a conditional update,
     * return whether it was successfully applied.
     * <p/>
     * For consistency, this method always returns {@code true} for
     * non-conditional queries (although there is no reason to call the method
     * in that case). This is also the case for conditional DDL statements
     * ({@code CREATE KEYSPACE... IF NOT EXISTS}, {@code CREATE TABLE... IF NOT EXISTS}),
     * for which Cassandra doesn't return an {@code [applied]} column.
     * <p/>
     * Note that, for versions of Cassandra strictly lower than 2.0.9 and 2.1.0-rc2,
     * a server-side bug (CASSANDRA-7337) causes this method to always return
     * {@code true} for batches containing conditional queries.
     *
     * @return if the query was a conditional update, whether it was applied.
     * {@code true} for other types of queries.
     * @see <a href="https://issues.apache.org/jira/browse/CASSANDRA-7337">CASSANDRA-7337</a>
     */
    public boolean wasApplied() {
        return wrapped.wasApplied();
    }
}
