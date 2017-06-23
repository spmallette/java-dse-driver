/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

/**
 * Defines a subprotocol to be used by the Java driver to communicate with DSE Graph.
 * The subprotocol is stacked on top of the Apache Cassandra Native Protocol, and allows
 * to handle the needs of specific DSE workloads.
 */
public enum GraphProtocol {
    /**
     * GraphSON1 is unsafe since it doesn't have an advanced typing format which causes type
     * loss when transferring data to the server or receiving data. We recommend using
     * {@link com.datastax.driver.dse.graph.GraphProtocol#GRAPHSON_2_0} instead.
     */
    GRAPHSON_1_0("graphson-1.0"),

    /**
     * Improvement of the GraphSON1 format that brings strong data-type information support.
     */
    GRAPHSON_2_0("graphson-2.0");

    private final String protocolReference;

    GraphProtocol(String protocolReference) {
        this.protocolReference = protocolReference;
    }

    public String getProtocolReference() {
        return protocolReference;
    }
}