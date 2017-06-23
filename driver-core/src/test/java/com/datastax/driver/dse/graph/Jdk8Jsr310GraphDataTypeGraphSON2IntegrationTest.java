/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.IgnoreJDK6Requirement;
import org.testng.annotations.DataProvider;

import java.util.ArrayList;
import java.util.List;

@IgnoreJDK6Requirement
@SuppressWarnings("Since15")
@DseVersion(value = "5.0.4", description = "GraphSON 2.0 requires DSE 5.0.4 or greater")
public class Jdk8Jsr310GraphDataTypeGraphSON2IntegrationTest extends Jdk8Jsr310GraphDataTypeIntegrationTest {

    @DataProvider
    public static Object[][] dataTypeSamples51() {
        // Somewhat complicated, but filters out the sample for Date that asserts a result as a string.  This
        // doesn't work for GraphSON2.0 since it uses precise types.
        Object[][] parent = Jdk8Jsr310GraphDataTypeIntegrationTest.dataTypeSamples51();

        List<Object[]> newSamples = new ArrayList<Object[]>();
        for (Object[] data : parent) {
            if (data[0].equals("Date()") && data[1] instanceof String) {
                continue;
            }
            newSamples.add(data);
        }

        return newSamples.toArray(new Object[][]{});
    }

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        cluster().getConfiguration().getGraphOptions().setGraphSubProtocol(GraphProtocol.GRAPHSON_2_0);
    }
}
