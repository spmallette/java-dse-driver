/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.dse.geometry.LineString;
import com.datastax.driver.dse.geometry.Polygon;
import com.google.common.net.InetAddresses;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.datastax.driver.dse.geometry.Utils.p;
import static com.datastax.driver.dse.graph.GraphAssertions.assertThat;

@DseVersion(major = 5.0)
public class GraphDataTypeIntegrationTest extends CCMGraphTestsSupport {

    AtomicInteger schemaCounter = new AtomicInteger(0);

    @DataProvider
    public Object[][] dataTypeSamples() {
        return new Object[][]{
                // Types that DSE supports.
                {"Boolean()", true},
                {"Boolean()", false},
                {"Smallint()", Short.MAX_VALUE},
                {"Smallint()", Short.MIN_VALUE},
                {"Smallint()", (short) 0},
                {"Smallint()", (short) 42},
                {"Int()", Integer.MAX_VALUE},
                {"Int()", Integer.MIN_VALUE},
                {"Int()", 0},
                {"Int()", 42},
                {"Bigint()", Long.MAX_VALUE},
                {"Bigint()", Long.MIN_VALUE},
                {"Bigint()", 0L},
                {"Double()", Double.MAX_VALUE},
                {"Double()", Double.MIN_VALUE},
                {"Double()", 0.0d},
                {"Double()", Math.PI},
                {"Float()", Float.MAX_VALUE},
                {"Float()", Float.MIN_VALUE},
                {"Float()", 0.0f},
                {"Text()", ""},
                {"Text()", "75"},
                {"Text()", "Lorem Ipsum"},
                // Inet, UUID, Date
                {"Inet()", InetAddresses.forString("127.0.0.1")},
                {"Inet()", InetAddresses.forString("0:0:0:0:0:0:0:1")},
                {"Inet()", InetAddresses.forString("2001:db8:85a3:0:0:8a2e:370:7334")},
                {"Uuid()", UUID.randomUUID()},
                {"Uuid()", UUIDs.timeBased()},
                // Timestamps
                {"Timestamp()", new Date(123)},
                // Blob
                // TODO: Test with base64 input when supported.
                //{"Blob()", "Hello World!".getBytes(Charsets.UTF_8)},
                // BigDecimal/BigInteger
                {"Decimal()", new BigDecimal("8675309.9998")},
                {"Varint()", new BigInteger("8675309")},
                // Geospatial types
                {"Point()", p(0, 1)},
                {"Point()", p(-5, 20)},
                {"Linestring()", new LineString(p(30, 10), p(10, 30), p(40, 40))},
                {"Polygon()", Polygon.builder()
                        .addRing(p(35, 10), p(45, 45), p(15, 40), p(10, 20), p(35, 10))
                        .addRing(p(20, 30), p(35, 35), p(30, 20), p(20, 30))
                        .build()}
        };
    }

    /**
     * Validates that a given data sample can be set as a parameter on Statement and interpreted as a given type for a vertex
     * property.  Does the following:
     * <ol>
     * <li>Define a property of the given type.</li>
     * <li>Create a vertex for each data sample with that property having that data sample value.</li>
     * <li>Validates that the result contains the created vertex with that property and that the property can be retrieved in the same format it was inserted in.</li>
     * <li>For completeness, queries the vertex and ensures the property value matches that which was inserted.</li>
     * </ol>
     *
     * @test_category dse:graph
     */
    @Test(groups = "short", dataProvider = "dataTypeSamples")
    public void should_create_and_retrieve_vertex_property(String type, Object input) {
        int id = schemaCounter.incrementAndGet();
        String vertexLabel = "vertex" + id;
        String propertyName = "prop" + id;
        GraphStatement addVertexLabelAndProperty = new SimpleGraphStatement(
                "schema.propertyKey(property)." + type + ".create()\n" +
                        "schema.vertexLabel(vertexLabel).properties(property).create()\n" +
                        "schema.vertexLabel(vertexLabel).index(property + 'Index').secondary().by(property).add()")
                .set("vertexLabel", vertexLabel)
                .set("property", propertyName);
        session().executeGraph(addVertexLabelAndProperty);

        GraphStatement addV = new SimpleGraphStatement("g.addV(label, vertexLabel, propertyName, val)")
                .set("vertexLabel", vertexLabel)
                .set("propertyName", propertyName)
                .set("val", input);
        GraphResultSet resultSet = session().executeGraph(addV);
        validateVertexResult(resultSet, vertexLabel, propertyName, input);

        // For completeness, retrieve the vertex and ensure the property value was maintained.
        GraphStatement getV = new SimpleGraphStatement("g.V().hasLabel(vertexLabel).has(propertyName, val).next()")
                .set("vertexLabel", vertexLabel)
                .set("propertyName", propertyName)
                .set("val", input);
        resultSet = session().executeGraph(getV);
        validateVertexResult(resultSet, vertexLabel, propertyName, input);
    }

    @SuppressWarnings("unchecked")
    private void validateVertexResult(GraphResultSet resultSet, String vertexLabel, String propertyName, Object expectedResult) {
        // Ensure the created vertex is returned and the property value matches what was provided.
        assertThat(resultSet.getAvailableWithoutFetching()).isEqualTo(1);
        Vertex v = resultSet.one().asVertex();
        VertexAssert a = assertThat(v).hasLabel(vertexLabel);

        // Validate using the appropriate asXXX method depending on the type of the class.
        if (expectedResult instanceof String) {
            a.hasProperty(propertyName, (String) expectedResult);
        } else if (expectedResult instanceof Integer) {
            a.hasProperty(propertyName, (Integer) expectedResult);
        } else if (expectedResult instanceof Boolean) {
            a.hasProperty(propertyName, (Boolean) expectedResult);
        } else if (expectedResult instanceof Long) {
            a.hasProperty(propertyName, (Long) expectedResult);
        } else if (expectedResult instanceof Double) {
            a.hasProperty(propertyName, (Double) expectedResult);
        }

        // Validate using the as(Clazz) method depending on the expectedClass.
        a.hasProperty(propertyName, expectedResult, (Class<Object>) expectedResult.getClass());
    }

}
