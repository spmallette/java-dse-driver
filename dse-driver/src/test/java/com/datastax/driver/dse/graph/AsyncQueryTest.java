/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.utils.DseVersion;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.*;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.datastax.driver.dse.graph.GraphAssertions.assertThat;
import static com.datastax.driver.dse.graph.GraphExtractors.asVertex;

@DseVersion(major = 5.0)
public class AsyncQueryTest extends CCMGraphTestsSupport {

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        executeGraph(
                GraphFixtures.makeStrict,
                GraphFixtures.allowScans,
                "schema.propertyKey('name').Text().create()",
                "schema.propertyKey('uuid').Uuid().create()",
                "schema.propertyKey('number').Double().create()",
                "schema.vertexLabel('person').properties('name', 'uuid', 'number').create()"
        );
    }

    /**
     * Validates that multiple addV statements can be executed simultaneously using
     * {@link com.datastax.driver.dse.DseSession#executeGraphAsync(GraphStatement)} and that the Vertex returned
     * from those queries properly represents the vertex created.  Finally, after all queries have been executed,
     * queries back all vertices and ensures they were all properly created.
     *
     * @test_category dse:graph
     */
    @Test(groups = "short")
    public void should_handle_multiple_vertex_creation_queries_simultaneously() throws Exception {
        int concurrency = 20;
        int requests = 100;
        final Semaphore permits = new Semaphore(concurrency);
        Random random = new Random();

        List<ListenableFuture<Vertex>> futures = Lists.newArrayListWithExpectedSize(requests);
        for (int i = 0; i < requests; i++) {
            if (!permits.tryAcquire(30, TimeUnit.SECONDS)) {
                throw new Exception("Could not acquire permit to send query within 30 seconds");
            }
            final String name = "User " + i;
            final UUID uuid = UUID.randomUUID();
            final Double number = random.nextDouble();

            SimpleGraphStatement addV = new SimpleGraphStatement("g.addV(label, 'person', 'name', name, 'uuid', uuid, 'number', number)")
                    .set("name", name)
                    .set("uuid", uuid.toString())
                    .set("number", number);

            ListenableFuture<GraphResultSet> future = session().executeGraphAsync(addV);

            // release semaphore on completion.
            future.addListener(new Runnable() {
                @Override
                public void run() {
                    permits.release();
                }
            }, MoreExecutors.sameThreadExecutor());

            // Evaluate vertex properties and ensure they match those that were inserted.
            futures.add(Futures.transform(future, new AsyncFunction<GraphResultSet, Vertex>() {

                @Override
                public ListenableFuture<Vertex> apply(GraphResultSet input) {
                    try {
                        GraphNode r = input.one();
                        assertThat(r).asVertex()
                                .hasProperty("name", name)
                                .hasProperty("uuid", uuid.toString())
                                .hasProperty("number", number);
                        return Futures.immediateFuture(r.asVertex());
                    } catch (Throwable t) {
                        return Futures.immediateFailedFuture(t);
                    }
                }
            }));
        }

        // Wait for reasonable time for vertices to be added.
        List<Vertex> addedVertices = Uninterruptibles.getUninterruptibly(Futures.allAsList(futures), 1, TimeUnit.MINUTES);

        // Retrieve all vertices and verify they were stored as expected.
        List<GraphNode> results = session().executeGraph("g.V().hasLabel('person')").all();
        assertThat(results).extracting(asVertex()).containsOnlyElementsOf(addedVertices);
    }
}
