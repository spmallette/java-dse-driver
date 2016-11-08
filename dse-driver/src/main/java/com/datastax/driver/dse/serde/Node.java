/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.serde;

import com.datastax.driver.core.exceptions.DriverException;
import com.google.common.reflect.TypeToken;

import java.util.Iterator;
import java.util.Map;

/**
 * A node in a tree-like structure, represented in a generic way.
 * <p>
 * This interface provide convenience methods to convert the node
 * that it represents into more specific types, such as
 * primitive types (boolean, string, int, long, double);
 * it also contains two generic
 * methods: {@link #as(Class)} and {@link #as(TypeToken)} that
 * can convert this node into any arbitrary Java type, provided
 * that the underlying serialization runtime has been correctly configured
 * to support the requested conversion.
 */
public interface Node {

    /**
     * Returns whether this node is {@code null}.
     *
     * @return {@code true} if this node is {@code null}, {@code false} otherwise.
     */
    boolean isNull();

    /**
     * Returns whether this node is an object.
     * <p>
     * Only one method out of {@link #isObject()}, {@link #isArray()} and
     * {@link #isValue()} should ever return {@code true} for one given node.
     *
     * @return {@code true} if this node is an object, {@code false} otherwise.
     */
    boolean isObject();

    /**
     * Returns whether this node is an array.
     * <p>
     * Only one method out of {@link #isObject()}, {@link #isArray()} and
     * {@link #isValue()} should ever return {@code true} for one given node.
     *
     * @return {@code true} if this node is an array, {@code false} otherwise.
     */
    boolean isArray();

    /**
     * Returns whether this node is a simple value,
     * i.e. either a string, a boolean, a number, or {@code null}.
     * <p>
     * Only one method out of {@link #isObject()}, {@link #isArray()} and
     * {@link #isValue()} should ever return {@code true} for one given node.
     *
     * @return whether this node is a simple value, {@code false} otherwise.
     */
    boolean isValue();

    /**
     * Returns all the field names of the current node, if it is an object,
     * or an empty iterator otherwise.
     *
     * @return a {@link Iterator} of all the field names of the current node.
     */
    Iterator<String> fieldNames();

    /**
     * Returns the size of the current node,
     * if it is an array, or {@code 0} otherwise.
     *
     * @return the size of the current node f it is an array,
     * or {@code 0} otherwise.
     */
    int size();

    /**
     * Returns this node as an integer.
     * <p>
     * If this node can not be converted to an int
     * (including structured types like Objects and Arrays),
     * {@code 0} will be returned; no exceptions will be thrown.
     *
     * @return an integer representation of this node,
     * or {@code 0} if this node can not be converted to an int.
     */
    int asInt();

    /**
     * Returns this node as a boolean.
     * <p>
     * If this node can not be converted to a boolean
     * (including structured types like Objects and Arrays),
     * {@code false} will be returned; no exceptions will be thrown.
     *
     * @return a boolean representation of this node,
     * or {@code false} if this node can not be converted to a boolean.
     */
    boolean asBoolean();

    /**
     * Returns this node as a long integer.
     *
     * @return a long integer representation of this node,
     * or {@code 0L} if this node is {@code null}.
     */
    long asLong();

    /**
     * Returns this node as a double.
     *
     * @return a double representation of this node,
     * or {@code 0.0D} if this node is {@code null}.
     */
    double asDouble();

    /**
     * Returns a valid String representation of this node,
     * if the node is a simple node (i.e. it is not an object nor an array),
     * otherwise returns an empty String.
     *
     * @return a String representation of this node,
     * or an empty string, if this node is not a simple node.
     */
    String asString();

    /**
     * Deserializes and returns this node as an instance of {@code Map<String, Object>}.
     *
     * @return this node converted into an instance of {@code Map<String, Object>}.
     * @throws DriverException if this node cannot be converted to a map.
     */
    Map<String, Object> asMap();

    /**
     * Deserializes and returns this node as an instance of {@code clazz}.
     * <p>
     * Before attempting such a conversion, there must be an appropriate converter
     * configured on the underlying serialization runtime.
     *
     * @param clazz the class to convert this node into.
     * @return this node converted into an instance of {@code clazz}.
     * @throws DriverException if this node cannot be converted to the given class.
     */
    <T> T as(Class<T> clazz);

    /**
     * Deserializes and returns this node as an instance of the given {@link TypeToken type}.
     * <p>
     * Before attempting such a conversion, there must be an appropriate converter
     * configured on the underlying serialization runtime.
     *
     * @param type the type to convert this node into.
     * @return this node converted into an instance of {@code type}.
     * @throws DriverException if this node cannot be converted to the given type.
     */
    <T> T as(TypeToken<T> type);

    /**
     * Returns the value of the specified field of an object node.
     * <p>
     * If this node is not an object (or it does not have a value
     * for the specified field name), or if there is no field with
     * such name, {@code null} is returned.
     * <p>
     * If the property value has been explicitly set to {@code null},
     * implementors may return a special "null node" instead of {@code null}.
     *
     * @param fieldName the field name to fetch.
     * @return a node containing the requested field value, or {@code null}
     * if it does not exist.
     */
    Node get(String fieldName);

    /**
     * Returns the element node at the specified {@code index} of an array node.
     * <p>
     * For all other node types, {@code null} is returned.
     * <p>
     * If {@code index} is out of bounds, (i.e. less than zero or {@code >= size()},
     * {@code null} is returned; no exception will be thrown.
     * <p>
     * If the requested element has been explicitly set to {@code null},
     * implementors may return a special "null node" instead of {@code null}.
     *
     * @param index the element index to fetch.
     * @return a node containing the requested element, or {@code null}
     * if it does not exist.
     */
    Node get(int index);

}
