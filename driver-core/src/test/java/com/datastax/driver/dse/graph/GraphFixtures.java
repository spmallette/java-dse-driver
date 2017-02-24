/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.VersionNumber;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

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

    private static String pointType(VersionNumber dseVersion) {
        Preconditions.checkNotNull(dseVersion);
        return (dseVersion.getMajor() == 5 && dseVersion.getMinor() == 0)
                ? "Point()"
                : "Point().withGeoBounds()";
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
                        "schema.propertyKey('place')." + pointType(dseVersion) + ".ifNotExists().create();",

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
                        "schema.propertyKey('coordinates')." + pointType(dseVersion) + ".create()\n" +
                        "schema.propertyKey('city').Text().create()\n" +
                        "schema.propertyKey('state').Text().create()\n" +
                        "schema.propertyKey('description').Text().create()\n" +
                        "schema.propertyKey('alias').Text().create()\n" +
                        "schema.vertexLabel('user').properties('full_name', 'coordinates', 'city', 'state', 'description').create()\n" +
                        "schema.vertexLabel('user').index('search').search().by('full_name').asString().by('coordinates').by('description').asText().by('alias').asString().add()",
                "g.addV('user').property('full_name', 'Paul Thomas Joe').property('city', 'Rochester').property('state', 'MN').property('coordinates', Geo.point(-92.46295, 44.0234)).property('description', 'Lives by the hospital').property('alias', 'mario')",
                "g.addV('user').property('full_name', 'George Bill Steve').property('city', 'Minneapolis').property('state', 'MN').property('coordinates', Geo.point(-93.266667, 44.9778)).property('description', 'A cold dude').property('alias', 'wario')",
                "g.addV('user').property('full_name', 'James Paul Joe').property('city', 'Chicago').property('state', 'IL').property('coordinates', Geo.point(-87.684722, 41.836944)).property('description', 'Likes to hang out').property('alias', 'bowser')",
                "g.addV('user').property('full_name', 'Jill Alice').property('city', 'Atlanta').property('state', 'GA').property('coordinates', Geo.point(-84.39, 33.755)).property('description', 'Enjoys a very nice cold coca cola').property('alias', 'peach')"
        );
    }
}
