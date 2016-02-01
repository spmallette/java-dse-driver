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

import com.datastax.driver.core.utils.DseVersion;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.datastax.driver.graph.Assertions.assertThat;

@DseVersion(major = 5.0)
public class SimpleGraphStatementIntegrationTest extends CCMGraphTestsSupport {

    AtomicInteger schemaCounter = new AtomicInteger(0);

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        executeGraph(GraphFixtures.modern);
    }

    @DataProvider
    public Object[][] dataTypeSamples() {
        return new Object[][]{
                // Types that DSE supports.
                // TODO: Should we add custom serializers for the types don't natively work with the JSON Mapper?
                // TODO: Most types work natively, but some like Geospatial, Inet Addresses and Dates don't.
                // TODO: This is out of happenstance, so we only test with types we know will work natively with
                // TODO: json until we implement explicit serializer/deserializers.
                // TODO: BigDecimal loses precision (Math.PI as an example), look into that.
                // TODO: Add temporal types (Date, Instant, Duration)
                {"java.math.BigDecimal", new String[]{"8675309.9998"}, String.class},
                {"java.math.BigInteger", new String[]{"8675309"}, String.class},
                {"String", new String[]{"", "75", "Lorem Ipsum"}, String.class},
                {"Long", new Long[]{Long.MAX_VALUE, Long.MIN_VALUE, 0L}, Long.class},
                {"Integer", new Integer[]{Integer.MAX_VALUE, Integer.MIN_VALUE, 0, 42}, Integer.class},
                {"Short", new Short[]{Short.MAX_VALUE, Short.MIN_VALUE, (short) 0, 42}, Short.class},
                {"Double", new Double[]{Double.MAX_VALUE, Double.MIN_VALUE, 0.0, Math.PI}, Double.class},
                {"Boolean", new Boolean[]{true, false}, Boolean.class},
                // TODO: Uncomment when DSP-8087 implemented.
                //{"org.apache.cassandra.db.marshal.geometry.Point", new Point[]{p(0, 1), p(-5, 20)}, Point.class},
                //{"org.apache.cassandra.db.marshal.geometry.LineString", new LineString[]{new LineString(p(30, 10), p(10, 30), p(40, 40))}, LineString.class},
                //{"org.apache.cassandra.db.marshal.geometry.Polygon",
                //        new Polygon[]{
                //                Polygon.builder()
                //                        .addRing(p(35, 10), p(45, 45), p(15, 40), p(10, 20), p(35, 10))
                //                        .addRing(p(20, 30), p(35, 35), p(30, 20), p(20, 30))
                //                        .build()},
                //        Polygon.class},
                // {"org.apache.cassandra.db.marshal.geometry.Circle", new Circle[]{new Circle(p(1, 2), 3)}, Circle.class},
                {"java.net.InetAddress", new String[]{"127.0.0.1", "0:0:0:0:0:0:0:1", "2001:db8:85a3:0:0:8a2e:370:7334"}, String.class},
                {"java.net.Inet4Address", new String[]{"127.0.0.1"}, String.class},
                {"java.net.Inet6Address", new String[]{"0:0:0:0:0:0:0:1", "2001:db8:85a3:0:0:8a2e:370:7334"}, String.class},
                {"java.util.UUID", new String[]{UUID.randomUUID().toString()}, String.class}
        };
    }

    private <T> void validateVertexResult(GraphResultSet resultSet, T data, Class<T> clazz, String vertexLabel, String propertyName) {
        // Ensure the created vertex is returned and the property value matches what was provided.
        assertThat(resultSet.getAvailableWithoutFetching()).isEqualTo(1);
        assertThat(resultSet.one())
                .asVertex()
                .hasLabel(vertexLabel)
                .hasProperty(propertyName, data, clazz);
    }

    /**
     * Validates that a given data sample can be set as a parameter on Statement and interpreted as a given type for a vertex
     * property.  Does the following:
     * <ol>
     * <li>Define a property of the given type.</li>
     * <li>Create a vertex for each data sample with that property having that data sample value.</li>
     * <li>Validates that the result contains the created vertex with that property and that the property can be retrieve dint eh same format it was inserted in.</li>
     * <li>For completeness, queries the vertex and ensures the property value matches that which was inserted.</li>
     * </ol>
     *
     * @test_category dse:graph
     *
     * @param clazz     The class that the property key's should should be.
     * @param data      The sample data to add as property values and use as parameters.
     * @param dataClazz The class implementation of the data sample.
     * @param <T>       The type of the data sample.
     */
    @Test(groups = "short", dataProvider = "dataTypeSamples")
    public <T> void should_create_and_retrieve_vertex_property(String clazz, T[] data, Class<T> dataClazz) {
        int id = schemaCounter.incrementAndGet();
        String vertexLabel = "vertex" + id;
        String propertyName = "prop" + id;
        String importStatement = clazz.contains(".") ? "import " + clazz + "\n" : "";
        GraphStatement addVertexLabelAndProperty = new SimpleGraphStatement(importStatement +
                "Schema schema = graph.schema()\n" +
                "schema.buildVertexLabel(vertexLabel).add()\n" +
                "schema.buildPropertyKey(property, " + clazz + ".class).add()")
                .set("vertexLabel", vertexLabel)
                .set("property", propertyName);
        session().executeGraph(addVertexLabelAndProperty);

        for (T value : data) {
            GraphStatement addV = new SimpleGraphStatement("g.addV(label, vertexLabel, propertyName, val)")
                    .set("vertexLabel", vertexLabel)
                    .set("propertyName", propertyName)
                    .set("val", value);
            GraphResultSet resultSet = session().executeGraph(addV);
            validateVertexResult(resultSet, value, dataClazz, vertexLabel, propertyName);

            // For completeness, retrieve the vertex and ensure the property value was maintained.
            GraphStatement getV = new SimpleGraphStatement("g.V().hasLabel(vertexLabel).has(propertyName, val).next()")
                    .set("vertexLabel", vertexLabel)
                    .set("propertyName", propertyName)
                    .set("val", value);
            resultSet = session().executeGraph(getV);
            validateVertexResult(resultSet, value, dataClazz, vertexLabel, propertyName);
        }
    }

    /**
     * Ensures that a {@link Vertex} id can be given as a parameter.  This facilitates the convenience of being able to
     * reference a specific vertex as part of a traversal, i.e.:
     * <p/>
     * <code>g.V(vertexId)</code>
     * <p/>
     * The test first retrieves an existing {@link Vertex} by looking it up based on it's label and properties and then
     * takes the returned {@link Vertex} id and uses it to query the vertex again and ensures the returned vertex
     * payload matches.
     *
     * @test_category dse:graph
     */
    @Test(groups = "short")
    public void should_use_vertex_id_as_parameter() {
        GraphResultSet resultSet = session().executeGraph(
                new SimpleGraphStatement("g.V().hasLabel('person').has('name', name)").set("name", "marko"));

        assertThat(resultSet.getAvailableWithoutFetching()).isEqualTo(1);
        Vertex marko = resultSet.one().asVertex();
        assertThat(marko).hasProperty("name", "marko");

        resultSet = session().executeGraph(new SimpleGraphStatement("g.V(myV)").set("myV", marko.getId()));
        assertThat(resultSet.getAvailableWithoutFetching()).isEqualTo(1);
        Vertex marko2 = resultSet.one().asVertex();

        // Ensure that the returned vertex is the same as the first.
        assertThat(marko2).isEqualTo(marko);
    }

    /**
     * Ensures that a {@link Edge} id can be given as a parameter.  This facilitates the convenience of being able to
     * reference a specific edge as part of a traversal, i.e.:
     * <p/>
     * <code>g.E(edgeId).inV()</code>
     * <p/>
     * The test first retrieves an existing {@link Edge} by looking it up based on a matching property and then
     * takes the returned {@link Edge} instance's id and uses it to query for the incoming {@link Vertex} and ensures
     * the returned vertex matches what is expected.
     *
     * @test_category dse:graph
     */
    @Test(groups = "short")
    public void should_use_edge_id_as_parameter() {
        GraphResultSet resultSet = session().executeGraph(
                new SimpleGraphStatement("g.E().has('weight', weight)").set("weight", 0.2));

        assertThat(resultSet.getAvailableWithoutFetching()).isEqualTo(1);
        Edge created = resultSet.one().asEdge();
        assertThat(created).hasProperty("weight", 0.2).hasInVLabel("software").hasOutVLabel("person");

        resultSet = session().executeGraph(new SimpleGraphStatement("g.E(myE).inV()").set("myE", created.getId()));
        assertThat(resultSet.getAvailableWithoutFetching()).isEqualTo(1);
        Vertex lop = resultSet.one().asVertex();

        assertThat(lop).hasLabel("software").hasProperty("name", "lop").hasProperty("lang", "java");
    }

    /**
     * Validates that a list can be provided as a parameter and used as part of the executed groovy code.
     * <p/>
     * Executes a script with a list that evaluates each member of the list and creates a vertex for it, with
     * each vertex sharing the same label.  It then queries for all vertices sharing that label and ensures there is a
     * vertex for each element of the given list.
     */
    @Test(groups = "short")
    public void should_use_list_as_a_parameter() {
        Collection<String> characters = Lists.newArrayList("Mario", "Luigi", "Toad", "Bowser", "Peach", "Wario", "Waluigi");

        SimpleGraphStatement createCharacters = new SimpleGraphStatement("" +
                "characters.each { character -> \n" +
                "    graph.addVertex(label, 'character', 'name', character);\n" +
                "}").set("characters", characters);

        session().executeGraph(createCharacters);

        GraphResultSet resultSet = session().executeGraph("g.V().hasLabel('character').values('name')");

        assertThat(resultSet.all()).extractingResultOf("asString").containsOnlyElementsOf(characters);
    }

    /**
     * Validates that a map of mixed types can be provided as a parameter and used as part of the executed groovy
     * code.
     * <p/>
     * Executes a script that evaluates the map representing traits of Albert Einstein and creates a 'scientist'
     * vertex labeled for it.  For each element of map.citizenship,
     * creates a vertex labeled 'country' for which an outgoing edge labeled 'had_citizenship' is created from the
     * scientist vertex to the country vertex.  It then queries that the scientist vertex was added properly and
     * that all countries of citizenship had a vertex created with an accompanying edge from the scientist as well.
     *
     * @test_category dse:graph
     */
    @Test(groups = "short")
    public void should_use_map_as_a_parameter() {
        String name = "Albert Einstein";
        int year = 1879;
        String field = "Physics";
        List<String> citizenship = Lists.newArrayList("Kingdom of Württemberg", "Switzerland", "Austria", "Germany", "United States");
        Map<String, Object> einsteinProps = ImmutableMap.<String, Object>builder()
                .put("name", name)
                .put("year_born", year)
                .put("citizenship", citizenship)
                .put("field", field)
                .build();

        // Create a vertex for Einstein, and then add a vertex for each country of citizenship and an outgoing
        // edge from Einstein to country he had citizenship in.
        SimpleGraphStatement addV = new SimpleGraphStatement("" +
                "Vertex scientist = graph.addVertex(label, 'scientist', 'name', m.name, 'year_born', m.year_born, 'field', m.field)\n" +
                "m.citizenship.each { c -> \n" +
                "    Vertex country = graph.addVertex(label, 'country', 'name', c);\n" +
                "    scientist.addEdge('had_citizenship', country);\n" +
                "}").set("m", einsteinProps);

        session().executeGraph(addV);

        // Ensure einstein was properly added.
        GraphResult result = session().executeGraph("g.V().hasLabel('scientist').has('name', name)",
                ImmutableMap.<String, Object>of("name", name)).one();
        assertThat(result)
                .asVertex()
                .hasLabel("scientist")
                .hasProperty("name", name)
                .hasProperty("year_born", year)
                .hasProperty("field", field);

        // Ensure each country vertex was added and an edge was created from it to einstein.
        GraphResultSet resultSet = session().executeGraph(
                "g.V(vId).outE('had_citizenship').inV().values('name')",
                ImmutableMap.<String, Object>of("vId", result.asVertex().getId()));

        List<GraphResult> results = resultSet.all();
        assertThat(results).extractingResultOf("asString").containsOnlyElementsOf(citizenship);
    }
}
