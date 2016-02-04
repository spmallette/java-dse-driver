/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.dse.geometry.Geometry;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.*;

/**
 * Helper methods that deal with JSON serialization and deserialization of Graph results.
 */
class GraphJsonUtils {

    /**
     * A deserializer for {@link Edge} instances.
     */
    static class EdgeDeserializer extends JsonDeserializer<Edge> {

        @Override
        public Edge deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
            checkEdge(jsonNode);
            return new Edge(
                    new GraphResult(jsonNode.get("id")),
                    jsonNode.get("label").asText(),
                    jsonNode.get("type").asText(),
                    transformEdgeProperties(jsonNode.get("properties")),
                    new GraphResult(jsonNode.get("inV")),
                    jsonNode.get("inVLabel").asText(),
                    new GraphResult(jsonNode.get("outV")),
                    jsonNode.get("outVLabel").asText()
            );
        }

    }

    /**
     * A deserializer for {@link Vertex} instances.
     */
    static class VertexDeserializer extends JsonDeserializer<Vertex> {

        @Override
        public Vertex deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
            checkVertex(jsonNode);
            return new Vertex(
                    new GraphResult(jsonNode.get("id")),
                    jsonNode.get("label").asText(),
                    jsonNode.get("type").asText(),
                    transformVertexProperties(jsonNode.get("properties")));
        }
    }

    static class PathDeserializer extends JsonDeserializer<Path> {
        @Override
        public Path deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);

            return new Path(
                    toStringSets(jsonNode, "labels"),
                    toGraphResults(jsonNode, "objects"));
        }

        private List<Set<String>> toStringSets(JsonNode jsonNode, String name) {
            ImmutableList.Builder<Set<String>> listBuilder = ImmutableList.builder();
            Iterator<JsonNode> outerElements = jsonNode.get(name).elements();
            while (outerElements.hasNext()) {
                JsonNode next = outerElements.next();
                ImmutableSet.Builder<String> setBuilder = ImmutableSet.builder();
                Iterator<JsonNode> innerElements = next.elements();
                while (innerElements.hasNext()) {
                    setBuilder.add(innerElements.next().asText());
                }
                listBuilder.add(setBuilder.build());
            }
            return listBuilder.build();
        }

        private List<GraphResult> toGraphResults(JsonNode jsonNode, String name) {
            ImmutableList.Builder<GraphResult> builder = ImmutableList.builder();
            Iterator<JsonNode> it = jsonNode.get(name).elements();
            while (it.hasNext()) {
                JsonNode next = it.next();
                builder.add(new GraphResult(next));
            }
            return builder.build();
        }
    }

    static class GraphResultSerializer extends JsonSerializer<GraphResult> {
        @Override
        public void serialize(GraphResult graphResult, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            jsonGenerator.writeTree(graphResult.getJsonNode());
        }
    }

    static class GeometrySerializer extends JsonSerializer<Geometry> {
        @Override
        public void serialize(Geometry value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
            jsonGenerator.writeString(value.asWellKnownText());
        }
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        SimpleModule graphModule = new SimpleModule("graph");
        graphModule.addDeserializer(Edge.class, new EdgeDeserializer());
        graphModule.addDeserializer(Vertex.class, new VertexDeserializer());
        graphModule.addDeserializer(Path.class, new PathDeserializer());
        graphModule.addSerializer(GraphResult.class, new GraphResultSerializer());
        graphModule.addSerializer(Geometry.class, new GeometrySerializer());
        OBJECT_MAPPER.registerModule(graphModule);
    }

    static final Function<Row, GraphResult> ROW_TO_GRAPH_RESULT = new Function<Row, GraphResult>() {
        @Override
        public GraphResult apply(Row row) {
            // Seems like sometimes traversals can return empty rows
            if (row != null) {
                String jsonString = row.getString("gremlin");
                try {
                    JsonNode jsonNode = OBJECT_MAPPER.readTree(jsonString).get("result");
                    return new GraphResult(jsonNode);
                } catch (IOException e) {
                    throw new DriverException("Could not parse the result returned by the Graph server as a JSON string : " + jsonString, e);
                }
            } else {
                return new GraphResult(JsonNodeFactory.instance.nullNode());
            }
        }
    };

    /**
     * Converts a map of named query parameters into a list
     * of (positional) query parameters, each of them
     * being composed of a small JSON string in the form:
     * {@code {"name" : "paramName", "value" : paramValue}};
     * this is the query parameter format expected by DSE Graph.
     * <p/>
     * TODO this is likely to evolve, see DSP-7660
     *
     * @param valuesMap the map of named query parameters.
     * @return a list of query parameters, as expected by DSE Graph.
     */
    static String convert(Map<String, Object> valuesMap) {
        String values;
        try {
            values = OBJECT_MAPPER.writeValueAsString(valuesMap);
        } catch (IOException e) {
            throw new DriverException(String.format("Cannot serialize parameter"));
        }
        return values;
    }

    static <T> T as(JsonNode jsonNode, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.treeToValue(jsonNode, clazz);
        } catch (JsonProcessingException e) {
            throw new DriverException("Cannot deserialize element", e);
        }
    }

    // The properties map is stored in a specific (weird) structure (Map<String, Array[Map<String, Object>]>)
    // This creates a map of the property's name as key and property's value as value as a Map<String, GraphResult>.
    static Map<String, GraphResult> transformVertexProperties(JsonNode jsonProps) {
        if (jsonProps == null) {
            return Maps.newHashMap();
        }
        Map<String, GraphResult> properties = new HashMap<String, GraphResult>();
        Iterator<Map.Entry<String, JsonNode>> jsonPropsIterator = jsonProps.fields();
        while (jsonPropsIterator.hasNext()) {
            Map.Entry<String, JsonNode> prop = jsonPropsIterator.next();
            properties.put(prop.getKey(), new GraphResult(prop.getValue().findValue("value")));
        }
        return properties;
    }

    static Map<String, GraphResult> transformEdgeProperties(JsonNode jsonProps) {
        if (jsonProps == null) {
            return Maps.newHashMap();
        }
        Map<String, GraphResult> properties = new HashMap<String, GraphResult>();
        Iterator<Map.Entry<String, JsonNode>> jsonPropsIterator = jsonProps.fields();
        while (jsonPropsIterator.hasNext()) {
            Map.Entry<String, JsonNode> prop = jsonPropsIterator.next();
            properties.put(prop.getKey(), new GraphResult(prop.getValue()));
        }
        return properties;
    }

    static void checkVertex(JsonNode jsonNode) {
        JsonNode type = jsonNode.findValue("type");
        if (type == null || !type.asText().equals("vertex")) {
            throw new DriverException("Trying to deserialize a Vertex from a JSON node that does not represent a Vertex");
        }
    }

    static void checkEdge(JsonNode jsonNode) {
        JsonNode type = jsonNode.findValue("type");
        if (type == null || !type.asText().equals("edge")) {
            throw new DriverException("Trying to deserialize an Edge from a JSON node that does not represent an Edge");
        }
    }

}
