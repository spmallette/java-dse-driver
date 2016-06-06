/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

/**
 * The representation of a property in DSE Graph.
 */
public interface Property {

    /**
     * Returns the property name.
     * Should never be {@code null}.
     *
     * @return The property name.
     */
    String getName();

    /**
     * Returns the property value.
     *
     * @return The property value.
     */
    GraphNode getValue();

    /**
     * Returns the property's parent {@link Element}.
     * Should never be {@code null}.
     *
     * @return The property's parent {@link Element}.
     */
    Element getParent();

}
