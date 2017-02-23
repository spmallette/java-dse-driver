/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
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
