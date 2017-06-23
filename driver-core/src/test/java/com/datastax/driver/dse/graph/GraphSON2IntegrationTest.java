/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.utils.DseVersion;

@DseVersion(value = "5.0.4", description = "GraphSON 2.0 requires DSE 5.0.4 or greater")
public class GraphSON2IntegrationTest extends GraphIntegrationTest {

    public GraphSON2IntegrationTest() {
        super(GraphProtocol.GRAPHSON_2_0);
    }
}