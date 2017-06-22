/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * We need this module because the TinkerGraph serialized type name is "gremlin:graph"
 * but is likely to change in the next or after DSE version to "tinker:graph" since the
 * change has been done in TinkerPop but we're not sure when DSE will update its TP version.
 * So that way we stay compatible whatever version of DSE is running.
 */
class GraphSON2TinkerDriverModule extends GraphSON2JacksonModule {
    GraphSON2TinkerDriverModule() {
        super("graph-graphson2tinker");
    }

    @Override
    public Map<Class<?>, String> getTypeDefinitions() {
        final ImmutableMap.Builder<Class<?>, String> builder = ImmutableMap.builder();

        builder.put(Map.class, "graph");

        return builder.build();
    }

    @Override
    public String getTypeNamespace() {
        return "tinker";
    }
}
