/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.utils.DseVersion;

@DseVersion("5.0")
public class GraphSON1IntegrationTest extends GraphIntegrationTest {

    public GraphSON1IntegrationTest() {
        super(GraphProtocol.GRAPHSON_1_0);
    }
}
