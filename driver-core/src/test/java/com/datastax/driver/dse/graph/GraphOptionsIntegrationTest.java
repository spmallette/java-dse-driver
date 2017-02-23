/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.utils.DseVersion;
import org.testng.annotations.Test;

@DseVersion("5.0.0")
public class GraphOptionsIntegrationTest extends CCMGraphTestsSupport {

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        executeGraph(GraphFixtures.modern);
    }

    /**
     * Ensures that if a non-existing graph name is used that an error is thrown and the driver handles it gracefully.
     * <p/>
     * As DSE currently returns a SERVER_ERROR there is currently not a good way to handle this, so this test is
     * disabled.
     *
     * @test_category dse:graph
     */
    @Test(groups = "short", enabled = false)
    public void should_handle_error_when_graph_name_doesnt_exist() {
        GraphOptions options = cluster().getConfiguration().getGraphOptions();
        String originalName = options.getGraphName();
        try {
            options.setGraphName("nonExisting");
            session().executeGraph("g.V()");
        } finally {
            options.setGraphName(originalName);
        }
    }

    // TODO tests for graph source and language.
}
