/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.CCMBridge;
import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.geometry.Point;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;

import static com.datastax.driver.dse.graph.GraphAssertions.assertThat;
import static com.datastax.driver.dse.graph.GraphFixtures.gods;

/**
 * Verifies that custom serializers and deserializers can be registered
 * and used, and that a completely customized domain model can be used
 * to model a graph.
 */
@DseVersion("5.0.0")
public class CustomSerdeIntegrationTest extends CCMGraphTestsSupport {

    @JsonSerialize(using = GodSerializer.class)
    @JsonDeserialize(using = GodDeserializer.class)
    static class God {
        Map<String, Object> id;
        String name;
        int age;
        Set<String> nicknames;
    }

    @JsonSerialize(using = MonsterSerializer.class)
    @JsonDeserialize(using = MonsterDeserializer.class)
    static class Monster {
        Map<String, Object> id;
        String name;
    }

    @JsonSerialize(using = BattleSerializer.class)
    @JsonDeserialize(using = BattleDeserializer.class)
    static class Battle {
        Map<String, Object> id;
        God winner = new God();
        Monster loser = new Monster();
        Date when;
        Location where;
    }

    @JsonDeserialize(using = LocationDeserializer.class)
    protected static class Location {
        final double x;
        final double y;

        Location(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    static class GodSerializer extends StdSerializer<God> {

        GodSerializer() {
            super(God.class);
        }

        @Override
        public void serialize(God value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.getCodec().writeValue(gen, value.id);
        }

    }

    static class GodDeserializer extends StdDeserializer<God> {

        GodDeserializer() {
            super(God.class);
        }

        @SuppressWarnings("unchecked")
        @Override
        public God deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.readValueAsTree();
            ObjectMapper objectMapper = (ObjectMapper) p.getCodec();
            God god = new God();
            god.id = objectMapper.treeToValue(node.get("id"), Map.class);
            god.name = node.get("properties").get("name").get(0).get("value").asText();
            god.age = node.get("properties").get("age").get(0).get("value").asInt();
            god.nicknames = new LinkedHashSet<String>();
            JsonNode nicknames = node.get("properties").get("nicknames");
            if (nicknames != null)
                for (int i = 0; i < nicknames.size(); i++)
                    god.nicknames.add(nicknames.get(i).get("value").asText());
            return god;
        }
    }

    static class MonsterSerializer extends StdSerializer<Monster> {

        MonsterSerializer() {
            super(Monster.class);
        }

        @Override
        public void serialize(Monster value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.getCodec().writeValue(gen, value.id);
        }

    }

    static class MonsterDeserializer extends StdDeserializer<Monster> {

        MonsterDeserializer() {
            super(Monster.class);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Monster deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.readValueAsTree();
            ObjectMapper objectMapper = (ObjectMapper) p.getCodec();
            Monster monster = new Monster();
            monster.id = objectMapper.treeToValue(node.get("id"), Map.class);
            monster.name = node.get("properties").get("name").get(0).get("value").asText();
            return monster;
        }

    }

    static class BattleSerializer extends StdSerializer<Battle> {

        BattleSerializer() {
            super(Battle.class);
        }

        @Override
        public void serialize(Battle value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.getCodec().writeValue(gen, value.id);
        }

    }

    static class BattleDeserializer extends StdDeserializer<Battle> {

        BattleDeserializer() {
            super(Battle.class);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Battle deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.readValueAsTree();
            ObjectMapper objectMapper = (ObjectMapper) p.getCodec();
            Battle battle = new Battle();
            battle.id = objectMapper.treeToValue(node.get("id"), Map.class);
            battle.loser.id = objectMapper.treeToValue(node.get("inV"), Map.class);
            battle.winner.id = objectMapper.treeToValue(node.get("outV"), Map.class);
            battle.when = objectMapper.treeToValue(node.get("properties").get("time"), Date.class);
            battle.where = objectMapper.treeToValue(node.get("properties").get("place"), Location.class);
            return battle;
        }
    }

    /**
     * Deserializer for Location.
     */
    protected static class LocationDeserializer extends StdDeserializer<Location> {

        public LocationDeserializer() {
            super(Location.class);
        }

        @Override
        public Location deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            // leverages the ability of Jackson to locate the appropriate deserializer for Point
            Point point = p.readValueAs(Point.class);
            return new Location(point.X(), point.Y());
        }
    }

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        executeGraph(gods(CCMBridge.getGlobalDSEVersion()));
    }

    @Test(groups = "short")
    public void should_deserialize_vertex_as_custom_type() throws Exception {
        GraphNode result = session().executeGraph("g.V().has('name', 'neptune').next()").one();
        God neptune = result.as(God.class);
        assertThat(neptune).isNotNull();
        assertThat(neptune.name).isEqualTo("neptune");
        assertThat(neptune.age).isEqualTo(4500);
        assertThat(neptune.nicknames).containsExactly("Neppy", "Flipper");
        // use the retrieved vertex to query again
        God neptune2 = session().executeGraph(new SimpleGraphStatement("g.V(v).next()").set("v", neptune)).one().as(God.class);
        assertThat(neptune2).isNotNull();
        assertThat(neptune2).isEqualToComparingFieldByField(neptune);
    }

    @Test(groups = "short")
    public void should_deserialize_edge_as_custom_type() throws Exception {

        List<GraphNode> results = session().executeGraph("g.V().has('name', 'hercules').outE('battled')").all();
        assertThat(results).hasSize(3);

        List<Battle> battles = Lists.transform(results, new Function<GraphNode, Battle>() {
            @Override
            public Battle apply(GraphNode input) {
                return input.as(Battle.class);
            }
        });

        battles = new ArrayList<Battle>(battles);
        Collections.sort(battles, new Comparator<Battle>() {
            @Override
            public int compare(Battle o1, Battle o2) {
                return o1.when.compareTo(o2.when);
            }
        });

        Battle herculesVsNemean = battles.get(0);
        assertThat(herculesVsNemean).isNotNull();
        assertThat(herculesVsNemean.when).isEqualTo(new Date(1));
        assertThat(herculesVsNemean.where).isEqualToComparingFieldByField(new Location(38.1, 23.7));

        Battle herculesVsHydra = battles.get(1);
        assertThat(herculesVsHydra).isNotNull();
        assertThat(herculesVsHydra.when).isEqualTo(new Date(2));
        assertThat(herculesVsHydra.where).isEqualToComparingFieldByField(new Location(37.7, 23.9));

        Battle herculesVsCerberus = battles.get(2);
        assertThat(herculesVsCerberus).isNotNull();
        assertThat(herculesVsCerberus.when).isEqualTo(new Date(12));
        assertThat(herculesVsCerberus.where).isEqualToComparingFieldByField(new Location(39, 22));

        // use the retrieved edge to query again

        Battle herculesVsHydra2 = session().executeGraph(new SimpleGraphStatement("g.E(e).next()").set("e", herculesVsHydra)).one().as(Battle.class);
        assertThat(herculesVsHydra2).isNotNull();
        assertThat(herculesVsHydra2).isEqualToComparingFieldByField(herculesVsHydra);

        God hercules = session().executeGraph(new SimpleGraphStatement("g.V(v).next()").set("v", herculesVsCerberus.winner)).one().as(God.class);
        assertThat(hercules).isNotNull();
        assertThat(hercules.name).isEqualTo("hercules");

        Monster hydra = session().executeGraph(new SimpleGraphStatement("g.V(v).next()").set("v", herculesVsHydra.loser)).one().as(Monster.class);
        assertThat(hydra).isNotNull();
        assertThat(hydra.name).isEqualTo("hydra");

    }

    @Test(groups = "short")
    public void should_deserialize_vertex_as_default_type() throws Exception {
        GraphNode result = session().executeGraph("g.V().has('name', 'neptune').next()").one();
        Vertex neptune = result.asVertex();
        assertThat(neptune).isNotNull();
        assertThat(neptune.getProperty("name").getValue().asString()).isEqualTo("neptune");
        assertThat(neptune.getProperty("age").getValue().asInt()).isEqualTo(4500);
        Iterator<VertexProperty> nicknames = neptune.getProperties("nicknames");
        assertThat(nicknames.next().getValue().asString()).isEqualTo("Neppy");
        assertThat(nicknames.next().getValue().asString()).isEqualTo("Flipper");
        // use the retrieved vertex to query again
        Vertex neptune2 = session().executeGraph(new SimpleGraphStatement("g.V(v).next()").set("v", neptune)).one().asVertex();
        assertThat(neptune2).isNotNull();
        assertThat(neptune2).isEqualTo(neptune);
    }

}
