/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.utils.DseVersion;

@DseVersion(value = "5.0.4", description = "GraphSON 2.0 requires DSE 5.0.4 or greater")
public class GraphDataTypeGraphSON2IntegrationTest extends GraphDataTypeIntegrationTest {

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        cluster().getConfiguration().getGraphOptions().setGraphSubProtocol(GraphProtocol.GRAPHSON_2_0);
    }

    @Override
    boolean filterType(String type) {
        // Filter timestamps, time and date as time behavior varies by JDK version and for JDK 6 timezone info is lost
        // causing in appropriate conversions.  They will be tested for JDK8 in Jdk8Jsr310 Tests.
        return type.equals("Timestamp()") || type.equals("Time()") || type.equals("Date()");
    }
}
