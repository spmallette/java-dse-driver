/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.util.Iterator;

/**
 * A result entity containing a graph query's result, wrapping a JSON node.
 */
public class GraphResult {

    private final JsonNode jsonNode;

    GraphResult(JsonNode jsonNode) {
        if (jsonNode == null) {
            this.jsonNode = JsonNodeFactory.instance.nullNode();
        } else {
            this.jsonNode = jsonNode;
        }
    }

    /**
     * Returns the encapsulated JSON node as an integer.
     *
     * @return an integer representation of the encapsulated JSON node,
     * or {@code 0} if the encapsulated JSON node is {@code null}.
     */
    public int asInt() {
        return this.jsonNode.asInt();
    }

    /**
     * Returns the encapsulated JSON node as a boolean.
     *
     * @return a boolean representation of the encapsulated JSON node,
     * or {@code false} if the encapsulated JSON node is {@code null}.
     */
    public boolean asBoolean() {
        return this.jsonNode.asBoolean();
    }

    /**
     * Returns the encapsulated JSON node as a long integer.
     *
     * @return a long integer representation of the encapsulated JSON node,
     * or {@code 0L} if the encapsulated JSON node is {@code null}.
     */
    public long asLong() {
        return this.jsonNode.asLong();
    }

    /**
     * Returns the encapsulated JSON node as a double.
     *
     * @return a double representation of the encapsulated JSON node,
     * or {@code 0.0D} if the encapsulated JSON node is {@code null}.
     */
    public double asDouble() {
        return this.jsonNode.asDouble();
    }

    /**
     * Returns the encapsulated JSON node as a String.
     *
     * @return a String representation of the encapsulated JSON node,
     * or {@code "null"} if the encapsulated JSON node is {@code null}.
     */
    public String asString() {
        return this.jsonNode.asText();
    }

    /**
     * Returns the encapsulated JSON node as a {@link Vertex}.
     *
     * @return a {@link Vertex} representation of the encapsulated JSON node,
     * or {@code null} if the encapsulated JSON node is {@code null}.
     */
    public Vertex asVertex() {
        return as(Vertex.class);
    }

    /**
     * Returns the encapsulated JSON node as an {@link Edge}.
     *
     * @return an {@link Edge} representation of the encapsulated JSON node,
     * or {@code null} if the encapsulated JSON node is {@code null}.
     */
    public Edge asEdge() {
        return as(Edge.class);
    }

    /**
     * Returns the encapsulated JSON node as a {@link Path}.
     *
     * @return a {@link Path} representation of the encapsulated JSON node,
     * or {@code null} if the encapsulated JSON node is {@code null}.
     */
    public Path asPath() {
        return as(Path.class);
    }

    /**
     * Returns whether the underlying JSON node is {@code null}.
     *
     * @return {@code true} if the underlying JSON node is {@code null}, {@code false} otherwise.
     */
    public boolean isNull() {
        return jsonNode.isNull();
    }


    /**
     * Returns whether the wrapped content is a map.
     *
     * @return {@code true} if the wrapped content is a JSON Map, {@code false} otherwise.
     */
    public boolean isMap() {
        return this.jsonNode.isObject();
    }

    /**
     * Returns whether the wrapped content is an array.
     *
     * @return {@code true} if the wrapped content is a JSON Array, {@code false} otherwise.
     */
    public boolean isArray() {
        return this.jsonNode.isArray();
    }

    /**
     * Returns all the keys of the current {@link GraphResult} if the content of the {@link GraphResult} is a map.
     *
     * @return a {@link java.util.Iterator} of all the keys of the content.
     */
    public Iterator<String> keys() {
        return this.jsonNode.fieldNames();
    }

    /**
     * Returns the number of objects in the wrapped content if the content is an array.
     *
     * @return an {@code int} for the size of the wrapped content.
     */
    public int size() {
        return this.jsonNode.size();
    }

    /**
     * Returns the content of the encapsulated JSON value, with the specified key.
     *
     * @param key the key of the property to return from the encapsulated JSON.
     * @return a {@link GraphResult} containing the JSON result. The content can be a
     * {@link com.fasterxml.jackson.databind.node.NullNode} if the key does not exist.
     */
    public GraphResult get(String key) {
        return new GraphResult(this.jsonNode.get(key));
    }

    /**
     * Returns the content of the encapsulated JSON value, with the specified index.
     *
     * @param index the index of the value to return from the encapsulated JSON.
     * @return a {@link GraphResult} containing the JSON result. The content can be a
     * {@link com.fasterxml.jackson.databind.node.NullNode} if the index does not exist.
     */
    public GraphResult get(int index) {
        return new GraphResult(this.jsonNode.get(index));
    }

    /**
     * Tries to return the enclosed JSON result as an instance of the {@link Class} in parameter.
     * <p/>
     *
     * Caution: this will only work for primitive JSON classes and nested maps and lists with primitive JSON types.
     * This can also work with the driver's Graph specific classes like {@link Vertex}, {@link Edge} and {@link Path}.
     * 
     * @param clazz the class to deserialize this object into. You can only provide JSON primitive types, {@link java.util.Map}s,
     *              {@link java.util.List}s and driver's Graph elements such as {@link Vertex}, {@link Edge} and {@link Path}.
     * @return the result deserialized and instantiated into the desired class.
     */
    public <T> T as(Class<T> clazz) {
        return GraphJsonUtils.as(jsonNode, clazz);
    }

    JsonNode getJsonNode() {
        return this.jsonNode;
    }

    @Override
    public String toString() {
        return this.jsonNode != null
                ? this.jsonNode.toString()
                : null;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other instanceof GraphResult) {
            GraphResult that = (GraphResult) other;
            return this.jsonNode.equals(that.getJsonNode());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return jsonNode.hashCode();
    }
}

