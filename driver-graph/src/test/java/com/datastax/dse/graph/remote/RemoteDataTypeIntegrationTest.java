/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph.remote;

import com.datastax.driver.core.utils.DseVersion;
import com.datastax.dse.graph.DataTypeIntegrationTest;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import static com.datastax.dse.graph.TinkerGraphAssertions.assertThat;

/**
 * {@link DataTypeIntegrationTest} implementation using a remote traversal source with the implicit TinkerPop API.
 */
@DseVersion(value = "5.0.3", description = "DSE 5.0.3 required for remote TinkerPop support")
public class RemoteDataTypeIntegrationTest extends DataTypeIntegrationTest {

    RemoteDataTypeIntegrationTest() {
        super(true);
    }

    @Override
    protected void create_and_retrieve_vertex_property(Object input) {
        int id = schemaCounter.incrementAndGet();
        String vertexLabel = "vertex" + id;
        String propertyName = "prop" + id;

        // Create the vertex.  Since we are in development mode, DSE should create the property schema with
        // the appropriate types.
        Vertex addV = g.addV(vertexLabel).property(propertyName, input).next();
        assertThat(addV)
                .hasLabel(vertexLabel)
                .hasProperty(propertyName, input);

        // For completeness, retrieve the vertex and ensure the property value as maintained.
        Vertex getV = g.V().hasLabel(vertexLabel).has(propertyName, input).next();
        assertThat(getV)
                .hasLabel(vertexLabel)
                .hasProperty(propertyName, input);
    }
}
