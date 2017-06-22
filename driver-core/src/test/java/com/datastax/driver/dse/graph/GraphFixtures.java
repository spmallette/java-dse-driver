/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.VersionNumber;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.testng.annotations.DataProvider;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A group of fixtures that may be useful in multiple tests.
 */
public class GraphFixtures {

    public static final String makeStrict =
            "schema.config().option('graph.schema_mode').set('production')";

    public static final String allowScans =
            "schema.config().option('graph.allow_scan').set('true')";

    /**
     * A single statement that builds the
     * <a href="http://tinkerpop.apache.org/docs/3.1.0-incubating/#intro">TinkerPop Modern</a> example graph.
     */
    public static final Collection<String> modern = Lists.newArrayList(
            makeStrict + "\n" +
                    allowScans + "\n" +
                    "schema.propertyKey('name').Text().ifNotExists().create();\n" +
                    "schema.propertyKey('age').Int().ifNotExists().create();\n" +
                    "schema.propertyKey('lang').Text().ifNotExists().create();\n" +
                    "schema.propertyKey('weight').Float().ifNotExists().create();\n" +
                    "schema.vertexLabel('person').properties('name', 'age').ifNotExists().create();\n" +
                    "schema.vertexLabel('software').properties('name', 'lang').ifNotExists().create();\n" +
                    "schema.edgeLabel('created').properties('weight').connection('person', 'software').ifNotExists().create();\n" +
                    "schema.edgeLabel('knows').properties('weight').connection('person', 'person').ifNotExists().create();",
            "Vertex marko = graph.addVertex(label, 'person', 'name', 'marko', 'age', 29);\n" +
                    "Vertex vadas = graph.addVertex(label, 'person', 'name', 'vadas', 'age', 27);\n" +
                    "Vertex lop = graph.addVertex(label, 'software', 'name', 'lop', 'lang', 'java');\n" +
                    "Vertex josh = graph.addVertex(label, 'person', 'name', 'josh', 'age', 32);\n" +
                    "Vertex ripple = graph.addVertex(label, 'software', 'name', 'ripple', 'lang', 'java');\n" +
                    "Vertex peter = graph.addVertex(label, 'person', 'name', 'peter', 'age', 35);\n" +
                    "marko.addEdge('knows', vadas, 'weight', 0.5f);\n" +
                    "marko.addEdge('knows', josh, 'weight', 1.0f);\n" +
                    "marko.addEdge('created', lop, 'weight', 0.4f);\n" +
                    "josh.addEdge('created', ripple, 'weight', 1.0f);\n" +
                    "josh.addEdge('created', lop, 'weight', 0.4f);\n" +
                    "peter.addEdge('created', lop, 'weight', 0.2f);");

    private static String geoType(String baseName, VersionNumber dseVersion) {
        Preconditions.checkNotNull(dseVersion);
        return (dseVersion.getMajor() == 5 && dseVersion.getMinor() == 0)
                ? baseName
                : baseName + ".withGeoBounds()";
    }

    private static String geoTypeWithBounds(String baseName, VersionNumber dseVersion, double lowerLimitX, double lowerLimitY, double higherLimitX, double higherLimitY) {
        Preconditions.checkNotNull(dseVersion);

        return (dseVersion.getMajor() == 5 && dseVersion.getMinor() == 0)
                ? baseName
                : baseName + String.format(".withBounds(%f, %f, %f, %f)", lowerLimitX, lowerLimitY, higherLimitX, higherLimitY);
    }

    /**
     * Builds the Graph of the Gods example graph.
     * see com.datastax.bdp.graph.example.GraphOfTheGodsFactory.
     */
    public static Collection<String> gods(VersionNumber dseVersion) {
        return Lists.newArrayList(

                makeStrict,

                allowScans,

                // schema - properties
                "schema.propertyKey('name').Text().ifNotExists().create();\n" +
                        "schema.propertyKey('age').Int().ifNotExists().create();\n" +
                        "schema.propertyKey('time').Timestamp().ifNotExists().create();\n" +
                        "schema.propertyKey('reason').Text().ifNotExists().create();\n" +
                        "schema.propertyKey('nicknames').Text().multiple().properties('time').ifNotExists().create();\n" +
                        "schema.propertyKey('place')." + geoType("Point()", dseVersion) + ".ifNotExists().create();",

                // schema - vertices
                "schema.vertexLabel('titan').properties('name', 'age').ifNotExists().create();\n" +
                        "schema.vertexLabel('location').properties('name').ifNotExists().create();\n" +
                        "schema.vertexLabel('god').properties('name', 'age', 'nicknames').ifNotExists().create();\n" +
                        "schema.vertexLabel('demigod').properties('name', 'age').ifNotExists().create();\n" +
                        "schema.vertexLabel('human').properties('name', 'age').ifNotExists().create();\n" +
                        "schema.vertexLabel('monster').properties('name').ifNotExists().create();",

                // schema - edges
                "schema.edgeLabel('father').connection('god','god').connection('god','titan').connection('demigod','god').ifNotExists().create();\n" +
                        "schema.edgeLabel('mother').connection('demigod','human').ifNotExists().create();\n" +
                        "schema.edgeLabel('battled').properties('time', 'place').connection('god','god').connection('demigod','monster').ifNotExists().create();\n" +
                        "schema.edgeLabel('lives').properties('reason').connection('god','location').connection('monster','location').ifNotExists().create();\n" +
                        "schema.edgeLabel('pet').connection('god','monster').ifNotExists().create();\n" +
                        "schema.edgeLabel('brother').connection('god','god').ifNotExists().create();\n",

                // indices
                "schema.vertexLabel('god').index('godsByName').secondary().by('name').add();\n" +
                        "schema.vertexLabel('god').index('godsByAge').secondary().by('age').add();\n" +
                        "schema.vertexLabel('demigod').index('battlesByTime').outE('battled').by('time').add();\n" +
                        "schema.vertexLabel('titan').index('titansByName').secondary().by('name').add();",

                // objects
                "Vertex saturn = graph.addVertex(T.label, 'titan', 'name', 'saturn', 'age', 10000);\n" +
                        "Vertex sky = graph.addVertex(T.label, 'location', 'name', 'sky');\n" +
                        "Vertex sea = graph.addVertex(T.label, 'location', 'name', 'sea');\n" +
                        "Vertex jupiter = graph.addVertex(T.label, 'god', 'name', 'jupiter', 'age', 5000);\n" +
                        "Vertex neptune = graph.addVertex(T.label, 'god', 'name', 'neptune', 'age', 4500);\n" +
                        "Vertex hercules = graph.addVertex(T.label, 'demigod', 'name', 'hercules', 'age', 30);\n" +
                        "Vertex alcmene = graph.addVertex(T.label, 'human', 'name', 'alcmene', 'age', 45);\n" +
                        "Vertex pluto = graph.addVertex(T.label, 'god', 'name', 'pluto', 'age', 4000);\n" +
                        "Vertex nemean = graph.addVertex(T.label, 'monster', 'name', 'nemean');\n" +
                        "Vertex hydra = graph.addVertex(T.label, 'monster', 'name', 'hydra');\n" +
                        "Vertex cerberus = graph.addVertex(T.label, 'monster', 'name', 'cerberus');\n" +
                        "Vertex tartarus = graph.addVertex(T.label, 'location', 'name', 'tartarus');\n" +
                        "jupiter.addEdge('father', saturn);\n" +
                        "jupiter.addEdge('lives', sky, 'reason', 'loves fresh breezes');\n" +
                        "jupiter.addEdge('brother', neptune);\n" +
                        "jupiter.addEdge('brother', pluto);\n" +
                        "neptune.addEdge('lives', sea).property('reason', 'loves waves');\n" +
                        "neptune.addEdge('brother', jupiter);\n" +
                        "neptune.addEdge('brother', pluto);\n" +
                        "neptune.addEdge('battled', neptune).property('time', Instant.ofEpochMilli(5));\n" +  //self-edge
                        "neptune.property('nicknames','Neppy','time', Instant.ofEpochMilli(22));\n" +
                        "neptune.property('nicknames','Flipper','time', Instant.ofEpochMilli(25));\n" +
                        "hercules.addEdge('father', jupiter);\n" +
                        "hercules.addEdge('mother', alcmene);\n" +
                        "hercules.addEdge('battled', nemean, 'time', Instant.ofEpochMilli(1), 'place', 'POINT(38.1 23.7)');\n" +
                        "hercules.addEdge('battled', hydra, 'time', Instant.ofEpochMilli(2), 'place', 'POINT(37.7 23.9)');\n" +
                        "hercules.addEdge('battled', cerberus, 'time', Instant.ofEpochMilli(12), 'place', 'POINT(39 22)');\n" +
                        "pluto.addEdge('brother', jupiter);\n" +
                        "pluto.addEdge('brother', neptune);\n" +
                        "pluto.addEdge('lives', tartarus, 'reason', 'no fear of death');\n" +
                        "pluto.addEdge('pet', cerberus);\n" +
                        "cerberus.addEdge('lives', tartarus);"
        );
    }

    /**
     * Builds a simple schema that provides for a vertex with a property with sub properties.
     */
    public static Collection<String> metaProps = Lists.newArrayList(
            makeStrict,
            allowScans,
            "schema.propertyKey('sub_prop').Text().create()\n" +
                    "schema.propertyKey('sub_prop2').Text().create()\n" +
                    "schema.propertyKey('meta_prop').Text().properties('sub_prop', 'sub_prop2').create()\n" +
                    "schema.vertexLabel('meta_v').properties('meta_prop').create()"
    );

    /**
     * Builds a simple schema that provides for a vertex with a multi-cardinality property.
     */
    public static Collection<String> multiProps = Lists.newArrayList(
            makeStrict,
            allowScans,
            "schema.propertyKey('multi_prop').Text().multiple().create()\n" +
                    "schema.vertexLabel('multi_v').properties('multi_prop').create()\n"
    );

    /**
     * A schema representing an address book with search enabled on name, description, and coordinates.
     */
    public static Collection<String> addressBook(VersionNumber dseVersion) {
        return Lists.newArrayList(
                makeStrict,
                allowScans,
                "schema.propertyKey('full_name').Text().create()\n" +
                        "schema.propertyKey('coordinates')." + geoType("Point()", dseVersion) + ".create()\n" +
                        "schema.propertyKey('linestringProp')." + geoType("Linestring()", dseVersion) + ".create()\n" +
                        "schema.propertyKey('polygonProp')." + geoType("Polygon()", dseVersion) + ".create()\n" +
                        "schema.propertyKey('city').Text().create()\n" +
                        "schema.propertyKey('state').Text().create()\n" +
                        "schema.propertyKey('description').Text().create()\n" +
                        "schema.propertyKey('alias').Text().create()\n" +
                        "schema.vertexLabel('user').properties('full_name', 'coordinates', 'city', 'state', 'description', 'alias').create()\n" +
                        "schema.vertexLabel('user').index('search').search().by('full_name').asString().by('coordinates').by('description').asText().by('alias').asString().add()\n" +
                        "schema.vertexLabel('user').index('searchLinestring').secondary().by('linestringProp').add()\n" +
                        "schema.vertexLabel('user').index('searchPolygon').secondary().by('polygonProp').add()\n",
                "g.addV('user').property('full_name', 'Paul Thomas Joe').property('city', 'Rochester').property('state', 'MN').property('coordinates', Geo.point(-92.46295, 44.0234)).property('description', 'Lives by the hospital').property('alias', 'mario')",
                "g.addV('user').property('full_name', 'George Bill Steve').property('city', 'Minneapolis').property('state', 'MN').property('coordinates', Geo.point(-93.266667, 44.9778)).property('description', 'A cold dude').property('alias', 'wario').property('linestringProp', 'LINESTRING (30 10, 10 30, 40 40)')",
                "g.addV('user').property('full_name', 'James Paul Joe').property('city', 'Chicago').property('state', 'IL').property('coordinates', Geo.point(-87.684722, 41.836944)).property('description', 'Likes to hang out').property('alias', 'bowser').property('polygonProp', 'POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))')",
                "g.addV('user').property('full_name', 'Jill Alice').property('city', 'Atlanta').property('state', 'GA').property('coordinates', Geo.point(-84.39, 33.755)).property('description', 'Enjoys a very nice cold coca cola').property('alias', 'peach')"
        );
    }

    @DataProvider
    public static Object[][] indexTypes() {
        return new Object[][]{
                {"search"},
                {"materialized"},
                {"secondary"}
        };
    }

    /**
     * A schema representing an address book with 3 properties (full_name_*, description_*, alias_*) created for
     * each type of index (search, secondary, materialized).
     */
    public static Collection<String> textIndices() {
        Object[][] providerIndexTypes = indexTypes();
        String[] indexTypes = new String[providerIndexTypes.length];
        for (int i = 0; i < providerIndexTypes.length; i++) {
            indexTypes[i] = (String)providerIndexTypes[i][0];
        }

        StringBuilder schema = new StringBuilder("");
        StringBuilder propertyKeys = new StringBuilder("");
        StringBuilder vertexLabel = new StringBuilder("schema.vertexLabel('user').properties(");
        StringBuilder indices = new StringBuilder("");
        StringBuilder vertex0 = new StringBuilder("g.addV('user')");
        StringBuilder vertex1 = new StringBuilder("g.addV('user')");
        StringBuilder vertex2 = new StringBuilder("g.addV('user')");
        StringBuilder vertex3 = new StringBuilder("g.addV('user')");

        ArrayList<String> propertyNames = new ArrayList<String>();
        for (String indexType : indexTypes) {
            propertyKeys.append(String.format("schema.propertyKey('full_name_%s').Text().create()\n" +
                    "schema.propertyKey('description_%s').Text().create()\n" +
                    "schema.propertyKey('alias_%s').Text().create()\n", indexType, indexType, indexType));

            propertyNames.add("'full_name_" + indexType + "'");
            propertyNames.add("'description_" + indexType + "'");
            propertyNames.add("'alias_" + indexType + "'");

            if (indexType.equals("search")) {
                indices.append("schema.vertexLabel('user').index('search').search().by('full_name_search').asString().by('description_search').asText().by('alias_search').asString().add()\n");
            } else {
                indices.append(String.format("schema.vertexLabel('user').index('by_full_name_%s').%s().by('full_name_%s').add()\n", indexType, indexType, indexType));
                indices.append(String.format("schema.vertexLabel('user').index('by_description_%s').%s().by('description_%s').add()\n", indexType, indexType, indexType));
                indices.append(String.format("schema.vertexLabel('user').index('by_alias_name_%s').%s().by('alias_%s').add()\n", indexType, indexType, indexType));
            }

            vertex0.append(String.format(".property('full_name_%s', 'Paul Thomas Joe').property('description_%s', 'Lives by the hospital').property('alias_%s', 'mario')", indexType, indexType, indexType));
            vertex1.append(String.format(".property('full_name_%s', 'George Bill Steve').property('description_%s', 'A cold dude').property('alias_%s', 'wario')", indexType, indexType, indexType));
            vertex2.append(String.format(".property('full_name_%s', 'James Paul Joe').property('description_%s', 'Likes to hang out').property('alias_%s', 'bowser')", indexType, indexType, indexType));
            vertex3.append(String.format(".property('full_name_%s', 'Jill Alice').property('description_%s', 'Enjoys a very nice cold coca cola').property('alias_%s', 'peach')", indexType, indexType, indexType));
        }

        vertexLabel.append(Joiner.on(", ").join(propertyNames));
        vertexLabel.append(").create()\n");

        schema.append(propertyKeys).append(vertexLabel).append(indices);

        return Lists.newArrayList(
                makeStrict,
                allowScans,
                schema.toString(),
                vertex0.toString(),
                vertex1.toString(),
                vertex2.toString(),
                vertex3.toString()
        );
    }

    /**
     * A schema representing an address book with search enabled on name, description, and coordinates.
     */
    public static Collection<String> geoIndices(VersionNumber dseVersion) {
        Object[][] providerIndexTypes = indexTypes();
        String[] indexTypes = new String[providerIndexTypes.length];
        for (int i = 0; i < providerIndexTypes.length; i++) {
            indexTypes[i] = (String)providerIndexTypes[i][0];
        }

        StringBuilder schema = new StringBuilder("schema.propertyKey('full_name').Text().create()\n");
        StringBuilder propertyKeys = new StringBuilder("");
        StringBuilder vertexLabel = new StringBuilder("schema.vertexLabel('user').properties(");
        StringBuilder indices = new StringBuilder("");
        StringBuilder vertex0 = new StringBuilder("g.addV('user').property('full_name', 'Paul Thomas Joe')");
        StringBuilder vertex1 = new StringBuilder("g.addV('user').property('full_name', 'George Bill Steve')");
        String vertex2 = "g.addV('user').property('full_name', 'James Paul Joe')";
        StringBuilder vertex3 = new StringBuilder("g.addV('user').property('full_name', 'Jill Alice')");

        ArrayList<String> propertyNames = new ArrayList<String>();
        propertyNames.add("'full_name'");

        for (String indexType : indexTypes) {
            propertyKeys.append(String.format("schema.propertyKey('pointPropWithBounds_%s').%s.create()\n", indexType, geoTypeWithBounds("Point()", dseVersion, 0, 0, 100, 100)));
            propertyKeys.append(String.format("schema.propertyKey('pointPropWithGeoBounds_%s').%s.create()\n", indexType, geoType("Point()", dseVersion)));

            propertyNames.add("'pointPropWithBounds_" + indexType + "'");
            propertyNames.add("'pointPropWithGeoBounds_" + indexType + "'");

            if (indexType.equals("search")) {
                indices.append(String.format("schema.vertexLabel('user').index('search').search().by('pointPropWithBounds_%s').withError(0.00001, 0.0).by('pointPropWithGeoBounds_%s').withError(0.00001, 0.0).add()\n", indexType, indexType));
            } else {
                indices.append(String.format("schema.vertexLabel('user').index('by_pointPropWithBounds_%s').%s().by('pointPropWithBounds_%s').add()\n", indexType, indexType, indexType));
                indices.append(String.format("schema.vertexLabel('user').index('by_pointPropWithGeoBounds_%s').%s().by('pointPropWithGeoBounds_%s').add()\n", indexType, indexType, indexType));
            }

            vertex0.append(String.format(".property('pointPropWithBounds_%s', 'POINT(40.0001 40)').property('pointPropWithGeoBounds_%s', 'POINT(40.0001 40)')", indexType, indexType));
            vertex1.append(String.format(".property('pointPropWithBounds_%s', 'POINT(40 40)').property('pointPropWithGeoBounds_%s', 'POINT(40 40)')", indexType, indexType));
            vertex3.append(String.format(".property('pointPropWithBounds_%s', 'POINT(30 30)').property('pointPropWithGeoBounds_%s', 'POINT(30 30)')", indexType, indexType));
        }

        vertexLabel.append(Joiner.on(", ").join(propertyNames));
        vertexLabel.append(").create()\n");

        schema.append(propertyKeys).append(vertexLabel).append(indices);

        return Lists.newArrayList(
                makeStrict,
                allowScans,
                schema.toString(),
                vertex0.toString(),
                vertex1.toString(),
                vertex2,
                vertex3.toString()
        );
    }
}
