/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph;

import org.testng.SkipException;
import org.testng.annotations.Test;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class DataTypeIntegrationTest extends CCMTinkerPopTestsSupport {
    protected AtomicInteger schemaCounter = new AtomicInteger(0);

    protected DataTypeIntegrationTest(boolean remote) {
        super(remote);
    }

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();

        // Set the schema mode to development as we should be able to depend on DSE graph to detect
        // the graphson types and create the appropriate property types.
        session().executeGraph("schema.config().option('graph.schema_mode').set('development')");
    }

    /**
     * Validates that a given data sample can be used as a property value for a Vertex and that when retrieved
     * the returned value is equivalent to the one inserted.   Does the following:
     * <ol>
     * <li>Create a vertex with a previously non-existent property.</li>
     * <li>Verifies that the created vertex's property value matches the input</li>
     * <li>For completeness, queries the vertex and ensures the property value matches that which was inserted.</li>
     * </ol>
     *
     * @param type  Unused (needed as provided by the data provider).
     * @param input The input property value to use.
     * @test_category dse:graph
     */
    @Test(groups = "short", dataProvider = "dataTypeSamples",
            dataProviderClass = com.datastax.driver.dse.graph.GraphDataTypeIntegrationTest.class)
    public void should_create_and_retrieve_vertex_property(@SuppressWarnings("unused") String type, Object input) {
        if (input instanceof Date) {
            // Special case since we can't request the resulting type like we can with the string API,
            // and since Instant is the native type for Timestamp(), we convert the Date to an Instant
            // here as that's the expected output.
            Instant output = ((Date) input).toInstant();
            create_and_retrieve_vertex_property(input, output);
        } else {
            create_and_retrieve_vertex_property(input, input);
        }
    }

    /**
     * Validates JDK8 specific types as property values.
     *
     * @test_category dse:graph
     */
    @Test(groups = "short", dataProvider = "dataTypeSamples",
            dataProviderClass = com.datastax.driver.dse.graph.Jdk8Jsr310GraphDataTypeIntegrationTest.class)
    public void should_create_and_retrieve_vertex_property_jdk8_types(@SuppressWarnings("unused") String type, Object input) {
        if (input instanceof ZonedDateTime) {
            throw new SkipException("ZonedDateTime is not supported (see DSP-11243)");
        }
        create_and_retrieve_vertex_property(input, input);
    }

    protected abstract void create_and_retrieve_vertex_property(Object input, Object output);
}
