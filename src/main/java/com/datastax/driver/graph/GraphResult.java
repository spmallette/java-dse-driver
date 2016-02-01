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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.util.Iterator;
import java.util.Set;

/**
 * A result entity containing a graph query's result, wrapping a JSON node.
 */
public class GraphResult {

    private final JsonNode jsonNode;

    GraphResult(JsonNode jsonNode) {
        if (jsonNode == null) {
            this.jsonNode = JsonNodeFactory.instance.nullNode();
        }
        else {
            this.jsonNode = jsonNode;
        }
    }

    /**
     * Returns the encapsulated JSON node as an integer.
     *
     * @return An integer representation of the encapsulated JSON node,
     * or {@code 0} if the encapsulated JSON node is {@code null}.
     */
    public int asInt() {
        return this.jsonNode.asInt();
    }

    /**
     * Returns the encapsulated JSON node as a boolean.
     *
     * @return A boolean representation of the encapsulated JSON node,
     * or {@code false} if the encapsulated JSON node is {@code null}.
     */
    public boolean asBoolean() {
        return this.jsonNode.asBoolean();
    }

    /**
     * Returns the encapsulated JSON node as a long integer.
     *
     * @return A long integer representation of the encapsulated JSON node,
     * or {@code 0L} if the encapsulated JSON node is {@code null}.
     */
    public long asLong() {
        return this.jsonNode.asLong();
    }

    /**
     * Returns the encapsulated JSON node as a double.
     *
     * @return A double representation of the encapsulated JSON node,
     * or {@code 0.0D} if the encapsulated JSON node is {@code null}.
     */
    public double asDouble() {
        return this.jsonNode.asDouble();
    }

    /**
     * Returns the encapsulated JSON node as a String.
     *
     * @return A String representation of the encapsulated JSON node,
     * or {@code "null"} if the encapsulated JSON node is {@code null}.
     */
    public String asString() {
        return this.jsonNode.asText();
    }

    /**
     * Returns the encapsulated JSON node as a {@link Vertex}.
     *
     * @return A {@link Vertex} representation of the encapsulated JSON node,
     * or {@code null} if the encapsulated JSON node is {@code null}.
     */
    public Vertex asVertex() {
        return as(Vertex.class);
    }

    /**
     * Returns the encapsulated JSON node as an {@link Edge}.
     *
     * @return An {@link Edge} representation of the encapsulated JSON node,
     * or {@code null} if the encapsulated JSON node is {@code null}.
     */
    public Edge asEdge() {
        return as(Edge.class);
    }

    /**
     * Returns the encapsulated JSON node as a {@link Path}.
     *
     * @return A {@link Path} representation of the encapsulated JSON node,
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
     * @return an {@link int} for the size of the wrapped content.
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

    <T> T as(Class<T> clazz) {
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

