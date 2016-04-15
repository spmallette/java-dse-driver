/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.geometry.LineString;
import com.datastax.driver.dse.geometry.Point;
import com.datastax.driver.dse.geometry.Polygon;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.assertj.core.api.iterable.Extractor;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.datastax.driver.dse.geometry.Utils.p;
import static com.datastax.driver.dse.graph.Assertions.assertThat;

@DseVersion(major = 5.0)
public class GraphDataTypeIntegrationTest extends CCMGraphTestsSupport {

    AtomicInteger schemaCounter = new AtomicInteger(0);

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        executeGraph(GraphFixtures.modern);
    }

    @DataProvider
    public Object[][] dataTypeSamples() {
        // property class, input values, expected output (if null, assume input data matches output).
        return new Object[][]{
                // Types that DSE supports.
                // TODO: This test is currently a bit awkward since not all types supported by DSE Server are supported
                // TODO: by the driver.  All unsupported types are written as String parameters and read back as String
                // TODO: parameters, with exception of Geometric types which can be serialized as parameters but not
                // TODO: deserialized using GraphResult.
                {new Boolean[]{true, false}, null, "Boolean()", Boolean.class},
                {new Integer[]{Integer.MAX_VALUE, Integer.MIN_VALUE, 0, 42}, null, "Int()", Integer.class},
                {new Short[]{Short.MAX_VALUE, Short.MIN_VALUE, 0, 42}, null, "Smallint()", Short.class},
                {new Long[]{Long.MAX_VALUE, Long.MIN_VALUE, 0L}, null, "Bigint()", Long.class},
                {new Float[]{Float.MAX_VALUE, Float.MIN_VALUE, 0.0f, (float)Math.PI}, null, "Float()", Float.class},
                {new Double[]{Double.MAX_VALUE, Double.MIN_VALUE, 0.0, Math.PI}, null, "Double()", Double.class},
                {new String[]{"8675309.9998"}, null, "Decimal()", String.class},
                {new String[]{"8675309"}, null, "Varint()", String.class},
                {new String[]{"2016-02-04T02:26:31.657Z"}, null, "Timestamp()", String.class},
                {new String[]{"P2DT3H4M"}, new String[]{"PT51H4M"}, "Duration()", String.class},
                // TODO: Reenable when DSP-9208 addressed.
                //{new String[]{"0xCAFE"}, null, "Blob()", String.class},
                {new String[]{"", "75", "Lorem Ipsum"}, null, "Text()", String.class},
                {new String[]{UUID.randomUUID().toString()}, null, "Uuid()", String.class},
                {new String[]{"127.0.0.1", "0:0:0:0:0:0:0:1", "2001:db8:85a3:0:0:8a2e:370:7334"}, null, "Inet()", String.class},
                {new Point[]{p(0, 1), p(-5, 20)}, new String[]{"POINT (0 1)", "POINT (-5 20)"}, "Point()", String.class},
                {new LineString[]{new LineString(p(30, 10), p(10, 30), p(40, 40))}, new String[]{"LINESTRING (30 10, 10 30, 40 40)"}, "Linestring()", String.class},
                {new Polygon[]{
                        Polygon.builder()
                                .addRing(p(35, 10), p(45, 45), p(15, 40), p(10, 20), p(35, 10))
                                .addRing(p(20, 30), p(35, 35), p(30, 20), p(20, 30))
                                .build()},
                        new String[]{"POLYGON ((35 10, 45 45, 15 40, 10 20, 35 10), (30 20, 20 30, 35 35, 30 20))"}, "Polygon()", String.class}
        };
    }

    private <T> void validateVertexResult(GraphResultSet resultSet, T expectedResult, String vertexLabel, String propertyName, Class<T> expectedClass) {
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
        a.hasProperty(propertyName, expectedResult, expectedClass);
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
     * @param data        The sample data to add as property values and use as parameters.
     * @param resultData  The expected data returned from querying.
     * @param type        The type function for identifying the property type.
     * @param resultClass The class to get the resulting data as.
     * @test_category dse:graph
     */
    @Test(groups = "short", dataProvider = "dataTypeSamples")
    public <T> void should_create_and_retrieve_vertex_property(Object[] data, T[] resultData, String type, Class<T> resultClass) {
        int id = schemaCounter.incrementAndGet();
        String vertexLabel = "vertex" + id;
        String propertyName = "prop" + id;
        GraphStatement addVertexLabelAndProperty = new SimpleGraphStatement(
                "schema.propertyKey(property)." + type + ".create()\n" +
                "schema.vertexLabel(vertexLabel).properties(property).create()")
                .set("vertexLabel", vertexLabel)
                .set("property", propertyName);
        session().executeGraph(addVertexLabelAndProperty);

        for (int i = 0; i < data.length; i++) {
            Object input = data[i];
            @SuppressWarnings("unchecked")
            T expectedResult = resultData != null ? resultData[i] : (T) input;
            GraphStatement addV = new SimpleGraphStatement("g.addV(label, vertexLabel, propertyName, val)")
                    .set("vertexLabel", vertexLabel)
                    .set("propertyName", propertyName)
                    .set("val", input);
            GraphResultSet resultSet = session().executeGraph(addV);
            validateVertexResult(resultSet, expectedResult, vertexLabel, propertyName, resultClass);

            // For completeness, retrieve the vertex and ensure the property value was maintained.
            GraphStatement getV = new SimpleGraphStatement("g.V().hasLabel(vertexLabel).has(propertyName, val).next()")
                    .set("vertexLabel", vertexLabel)
                    .set("propertyName", propertyName)
                    .set("val", input);
            resultSet = session().executeGraph(getV);
            validateVertexResult(resultSet, expectedResult, vertexLabel, propertyName, resultClass);
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
     * A sanity check that ensures that we can deserialize a Vertex Id as a Map using {@link GraphResult#as(Class)}.
     * This test could break in the future if the format of a Vertex ID changes from a Map to something else.
     *
     * @jira_ticket JAVA-1080
     * @test_category dse:graph
     */
    @Test(groups = "short")
    public void should_deserialize_vertex_id_as_map() {
        GraphResultSet resultSet = session().executeGraph(
                new SimpleGraphStatement("g.V().hasLabel('person').has('name', name)").set("name", "marko"));

        assertThat(resultSet.getAvailableWithoutFetching()).isEqualTo(1);
        Vertex marko = resultSet.one().asVertex();
        assertThat(marko).hasProperty("name", "marko");

        @SuppressWarnings("unchecked")
        Map<String, Object> deserializedId = (Map<String, Object>) marko.getId().as(Map.class);
        assertThat(deserializedId.keySet()).containsExactly(Iterators.toArray(marko.getId().keys(), String.class));

        Iterator<String> keys = marko.getId().keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = deserializedId.get(key);
            assertThat(value).isEqualTo(marko.getId().get(key).as(value.getClass()));
        }
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
        GraphStatement schemaStmt = new SimpleGraphStatement("schema.vertexLabel('character').properties('name').create();");
        session().executeGraph(schemaStmt);

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
        GraphStatement schemaStmt = new SimpleGraphStatement("" +
                "schema.propertyKey('year_born').Text().create()\n" +
                "schema.propertyKey('field').Text().create()\n" +
                "schema.vertexLabel('scientist').properties('name', 'year_born', 'field').create()\n" +
                "schema.vertexLabel('country').properties('name').create()\n" +
                "schema.edgeLabel('had_citizenship').connection('scientist', 'country').create()");
        session().executeGraph(schemaStmt);

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

    /**
     * Ensures that a traversal that returns an object with labels can be properly represented as graph result.
     * <p/>
     * Executes a vertex traversal that binds label 'a' and 'b' to vertex properties and label 'c' to vertices that
     * have edges from that vertex.
     *
     * @test_category dse:graph
     */
    @Test(groups = "short")
    public void should_handle_result_object_of_mixed_types() {
        // find all software vertices and select name, language, and find all vertices that created such software.
        GraphResultSet rs = session().executeGraph("g.V().hasLabel('software').as('a', 'b', 'c')." +
                "select('a','b','c')." +
                "by('name')." +
                "by('lang')." +
                "by(__.in('created').fold())");


        assertThat(rs.getAvailableWithoutFetching()).isEqualTo(2);
        List<GraphResult> results = rs.all();

        // Ensure that we got 'lop' and 'ripple' for property a.
        assertThat(results).extracting(new Extractor<GraphResult, String>() {
            @Override
            public String extract(GraphResult input) {
                return input.get("a").asString();
            }
        }).containsOnly("lop", "ripple");

        for (GraphResult result : results) {
            // The row should represent a map with a, b, and c keys.
            assertThat(result.isMap()).isTrue();
            assertThat(result.keys()).containsOnlyOnce("a", "b", "c");
            // 'e' should not exist, thus it should be null.
            assertThat(result.get("e").isNull()).isTrue();
            // both software are written in java.
            assertThat(result.get("b").isNull()).isFalse();
            assertThat(result.get("b").asString()).isEqualTo("java");
            GraphResult c = result.get("c");
            assertThat(c.isArray()).isTrue();
            if (result.get("a").asString().equals("lop")) {
                // 'c' should contain marko, josh, peter.
                // Ensure we have three vertices.
                assertThat(c.size()).isEqualTo(3);
                List<Vertex> vertices = Lists.newArrayList(c.get(0).asVertex(), c.get(1).asVertex(), c.get(2).asVertex());
                assertThat(vertices).extracting(new Extractor<Vertex, String>() {
                    @Override
                    public String extract(Vertex input) {
                        return input.getProperties().get("name").asString();
                    }
                }).containsOnly("marko", "josh", "peter");
            } else {
                // ripple, 'c' should contain josh.
                // Ensure we have 1 vertex.
                assertThat(c.size()).isEqualTo(1);
                Vertex vertex = c.get(0).asVertex();
                assertThat(vertex).hasProperty("name", "josh");
            }
        }
    }

    /**
     * Ensures that a traversal that returns a subgraph can be properly deserialized as a graph result.
     * <p/>
     * A subgraph should contain 2 members, vertices and edges, with each being a list of the appropriate types.
     *
     * @test_category dse:graph
     */
    @Test(groups = "short")
    public void should_handle_subgraph() {
        GraphResultSet rs = session().executeGraph("g.E().hasLabel('knows').subgraph('subGraph').cap('subGraph')");

        assertThat(rs.getAvailableWithoutFetching()).isEqualTo(1);

        GraphResult result = rs.one();
        assertThat(result)
                .hasChild("edges")
                .hasChild("vertices");

        // There should only be 2 edges as there are only 2 knows edges (marko knows josh and vadas)
        GraphResult edges = result.get("edges");
        assertThat(edges.size()).isEqualTo(2);
        // Ensure edges can be deserialized as a list and that we can get each value as an Edge.
        for (int i = 0; i < edges.size(); i++) {
            GraphResult edge = edges.get(i);
            assertThat(edge).asEdge();
        }

        // There should only be 3 vertices (marko, josh, and vadas).
        GraphResult vertices = result.get("vertices");
        assertThat(vertices.size()).isEqualTo(3);
        // Ensure vertices can be deserialized as a list and that we can get each value as a Vertex.
        for (int i = 0; i < vertices.size(); i++) {
            GraphResult vertex = vertices.get(i);
            assertThat(vertex).asVertex();
        }
    }

    /**
     * Ensures an traversal that yields no results is properly parsed and returned.
     *
     * @test_category dse:graph
     */
    @Test(groups = "short")
    public void should_return_zero_results() {
        GraphResultSet rs = session().executeGraph("g.V().hasLabel('notALabel')");
        assertThat(rs.getAvailableWithoutFetching()).isZero();
    }
}
