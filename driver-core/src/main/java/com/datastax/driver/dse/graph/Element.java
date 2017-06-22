/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;


import java.util.Iterator;
import java.util.Set;

/**
 * The representation of an element in DSE Graph.
 * <p>
 * Elements in DSE Graph can be of three specific types:
 * {@link Vertex}, {@link Edge} and {@link VertexProperty}.
 */
public interface Element {

    /**
     * Returns the element's identifier.
     * Should never be {@code null}.
     *
     * @return the element's identifier.
     */
    GraphNode getId();

    /**
     * Returns the element's label.
     * Should never be {@code null}.
     *
     * @return the element's label.
     */
    String getLabel();

    /**
     * Returns the set of property names for this element,
     * or an empty set, if this element has no properties.
     * <p>
     * The returned set is immutable.
     *
     * @return the set of property names for this element.
     */
    Set<String> getPropertyNames();

    /**
     * Returns the first property of this element
     * that has the given {@code name},
     * or {@code null}, if such a property name
     * does not exist.
     * <p>
     * If more than one property of this element
     * has the given name, which one will be returned
     * is unspecified.
     *
     * @return the first property of this element
     * that has the given {@code name}.
     */
    Property getProperty(String name);

    /**
     * Returns all the properties of this element
     * that have the given {@code name},
     * or an empty iterator, if such a property name
     * does not exist.
     * <p>
     * The returned iterator is immutable.
     *
     * @return all the properties of this element
     * that have the given {@code name}.
     */
    Iterator<? extends Property> getProperties(String name);

    /**
     * Returns an iterator over the element's properties,
     * or an empty iterator, if the element does not have any property.
     * <p>
     * The returned iterator is immutable.
     *
     * @return an iterator over the element's properties.
     */
    Iterator<? extends Property> getProperties();
}
