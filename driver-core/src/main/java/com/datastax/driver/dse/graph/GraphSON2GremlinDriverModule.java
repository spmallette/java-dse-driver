/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class GraphSON2GremlinDriverModule extends GraphSON2JacksonModule {

    GraphSON2GremlinDriverModule() {
        super("graph-graphson2gremlin");
        addSerializer(Integer.class, new IntegerGraphSONSerializer());
        addSerializer(Double.class, new DoubleGraphSONSerializer());

        addDeserializer(Vertex.class, new VertexGraphSON2Deserializer());
        addDeserializer(VertexProperty.class, new VertexPropertyGraphSON2Deserializer());
        addDeserializer(Property.class, new PropertyGraphSON2Deserializer());
        addDeserializer(Edge.class, new EdgeGraphSON2Deserializer());
        addDeserializer(Path.class, new PathGraphSON2Deserializer());
    }

    @Override
    public Map<Class<?>, String> getTypeDefinitions() {
        // Override the TinkerPop classes' types.
        final ImmutableMap.Builder<Class<?>, String> builder = ImmutableMap.builder();
        builder.put(Integer.class, "Int32");
        builder.put(Long.class, "Int64");
        builder.put(Double.class, "Double");
        builder.put(Float.class, "Float");

        builder.put(Vertex.class, "Vertex");
        builder.put(VertexProperty.class, "VertexProperty");
        builder.put(Property.class, "Property");
        builder.put(Edge.class, "Edge");
        builder.put(Path.class, "Path");
        builder.put(List.class, "Tree");

        return builder.build();
    }

    @Override
    public String getTypeNamespace() {
        // Override the classes from the TinkerPop domain.
        return "g";
    }


    final static class IntegerGraphSONSerializer extends StdScalarSerializer<Integer> {
        IntegerGraphSONSerializer() {
            super(Integer.class);
        }

        @Override
        public void serialize(final Integer integer, final JsonGenerator jsonGenerator,
                              final SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeNumber(((Integer) integer).intValue());
        }
    }

    final static class DoubleGraphSONSerializer extends StdScalarSerializer<Double> {
        DoubleGraphSONSerializer() {
            super(Double.class);
        }

        @Override
        public void serialize(final Double doubleValue, final JsonGenerator jsonGenerator,
                              final SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeNumber(doubleValue);
        }
    }

    abstract static class ParentGremlinGraphSON2Deserializer<T extends Element> extends StdDeserializer<T> {

        T parent;

        protected ParentGremlinGraphSON2Deserializer(Class<T> clazz) {
            super(clazz);
        }

        @Override
        public T deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            parent = newObject();
            JsonParser contextualParser = new GremlinContextualJsonParser(jsonParser, parent);

            contextualParser.nextToken();
            // This will automatically parse all typed stuff.
            @SuppressWarnings("unchecked")
            final Map<String, Object> mapData = deserializationContext.readValue(contextualParser, Map.class);

            return initializeObject(mapData, parent);
        }

        abstract T newObject();

        abstract T initializeObject(Map<String, Object> data, T objectToInitialize);
    }

    abstract static class ParentChildGremlinGraphSON2Deserializer<T extends Element> extends ParentGremlinGraphSON2Deserializer<T> {

        Element parent;

        protected ParentChildGremlinGraphSON2Deserializer(Class<T> clazz) {
            super(clazz);
        }

        @Override
        public T deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            parent = ContextualDelegateParser.class.isAssignableFrom(jsonParser.getClass())
                    ? (Element) ((ContextualDelegateParser) jsonParser).getContext().get(0)
                    : null;

            T currentParent = newObject();
            JsonParser contextualParser = new GremlinContextualJsonParser(jsonParser, currentParent);

            contextualParser.nextToken();
            // This will automatically parse all typed stuff.
            @SuppressWarnings("unchecked")
            final Map<String, Object> mapData = deserializationContext.readValue(contextualParser, Map.class);

            return initializeObject(mapData, currentParent);
        }
    }

    abstract static class ChildGremlinGraphSON2Deserializer<T> extends StdDeserializer<T> {

        protected Element parent;

        protected ChildGremlinGraphSON2Deserializer(Class<T> clazz) {
            super(clazz);
        }

        @Override
        public T deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            parent = ContextualDelegateParser.class.isAssignableFrom(jsonParser.getClass())
                    ? (Element) ((ContextualDelegateParser) jsonParser).getContext().get(0)
                    : null;

            jsonParser.nextToken();
            // This will automatically parse all typed stuff.
            @SuppressWarnings("unchecked")
            final Map<String, Object> mapData = deserializationContext.readValue(jsonParser, Map.class);

            return createObject(mapData);
        }

        abstract T createObject(Map<String, Object> data);
    }

    final static class VertexGraphSON2Deserializer extends ParentGremlinGraphSON2Deserializer<Vertex> {

        VertexGraphSON2Deserializer() {
            super(Vertex.class);
        }

        @Override
        Vertex newObject() {
            return new DefaultVertex();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Vertex initializeObject(final Map<String, Object> map, Vertex element) {
            DefaultVertex vertex = (DefaultVertex) element;

            vertex.id = new ObjectGraphNode(map.get("id"));
            vertex.label = map.get("label").toString();

            if (map.containsKey("properties")) {
                ImmutableMultimap.Builder<String, GraphNode> builder = ImmutableMultimap.builder();
                for (Map.Entry<String, List<VertexProperty>> propertyEntry : ((Map<String, List<VertexProperty>>) map.get("properties")).entrySet()) {
                    for (VertexProperty vp : propertyEntry.getValue()) {
                        builder.put(propertyEntry.getKey(), new ObjectGraphNode(vp));
                    }
                }
                vertex.properties = builder.build();
            }

            return vertex;
        }
    }

    final static class VertexPropertyGraphSON2Deserializer extends ParentChildGremlinGraphSON2Deserializer<VertexProperty> {

        VertexPropertyGraphSON2Deserializer() {
            super(VertexProperty.class);
        }

        @Override
        VertexProperty newObject() {
            return new DefaultVertexProperty();
        }

        @SuppressWarnings("unchecked")
        @Override
        public VertexProperty initializeObject(Map<String, Object> map, VertexProperty element) {
            DefaultVertexProperty vp = (DefaultVertexProperty) element;

            vp.id = new ObjectGraphNode(map.get("id"));
            // DSE Graph includes VP's labels by default, but in theory the field in
            // the protocol in TinkerPop is optional, so we might as well be prepared.
            if (map.containsKey("label")) {
                vp.label = map.get("label").toString();
            }
            vp.value = new ObjectGraphNode(map.get("value"));

            if (map.containsKey("properties")) {
                ImmutableMultimap.Builder<String, GraphNode> builder = ImmutableMultimap.builder();
                for (Map.Entry<String, Object> propertyEntry : ((Map<String, Object>) map.get("properties")).entrySet()) {
                    DefaultProperty prop = new DefaultProperty();
                    prop.name = propertyEntry.getKey();
                    prop.value = new ObjectGraphNode(propertyEntry.getValue());
                    prop.parent = vp;
                    builder.putAll(propertyEntry.getKey(), new ObjectGraphNode(prop));
                }
                vp.properties = builder.build();
            }

            vp.parent = (Vertex)parent;

            return vp;
        }
    }

    final static class PropertyGraphSON2Deserializer extends ChildGremlinGraphSON2Deserializer<Property> {

        PropertyGraphSON2Deserializer() {
            super(Property.class);
        }

        @Override
        public Property createObject(Map<String, Object> map) {
            DefaultProperty prop = new DefaultProperty();

            prop.name = map.get("key").toString();
            prop.value = new ObjectGraphNode(map.get("value"));
            prop.parent = parent;

            return prop;
        }
    }

    final static class EdgeGraphSON2Deserializer extends ParentGremlinGraphSON2Deserializer<Edge> {

        EdgeGraphSON2Deserializer() {
            super(Edge.class);
        }

        @Override
        Edge newObject() {
            return new DefaultEdge();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Edge initializeObject(Map<String, Object> map, Edge element) {
            DefaultEdge edge = (DefaultEdge) element;

            edge.id = new ObjectGraphNode(map.get("id"));
            edge.label = map.get("label").toString();

            if (map.containsKey("properties")) {
                ImmutableMultimap.Builder<String, GraphNode> builder = ImmutableMultimap.builder();
                for (Map.Entry<String, Property> propertyEntry : ((Map<String, Property>) map.get("properties")).entrySet()) {
                    builder.putAll(propertyEntry.getKey(), new ObjectGraphNode(propertyEntry.getValue()));
                }
                edge.properties = builder.build();
            }

            edge.inV = new ObjectGraphNode(map.get("inV"));
            // inVLabel might become redundant, we want to make sure we're resilient there.
            if (map.containsKey("inVLabel"))
                edge.inVLabel = map.get("inVLabel").toString();

            edge.outV = new ObjectGraphNode(map.get("outV"));
            // outVLabel might become redundant, we want to make sure we're resilient there.
            if (map.containsKey("outVLabel"))
                edge.outVLabel = map.get("outVLabel").toString();

            return edge;
        }
    }

    final static class PathGraphSON2Deserializer extends AbstractObjectDeserializer<Path> {

        PathGraphSON2Deserializer() {
            super(Path.class);
        }

        @Override
        public Path createObject(Map<String, Object> map) {
            DefaultPath path = new DefaultPath();

            path.labels = new ArrayList<Set<String>>();

            @SuppressWarnings("unchecked")
            List<List<String>> labels = (List<List<String>>) map.get("labels");
            for (List<String> labelsSet : labels) {
                path.labels.add(new HashSet<String>(labelsSet));
            }

            path.objects = new ArrayList<GraphNode>();

            @SuppressWarnings("unchecked")
            List<Object> objects = (List<Object>) map.get("objects");
            for (Object object : objects) {
                path.objects.add(new ObjectGraphNode(object));
            }
            return path;
        }
    }
}
