/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

class GraphSON2GremlinGraphDriverModule extends GraphSON2JacksonModule {
    GraphSON2GremlinGraphDriverModule() {
        super("graph-graphson2gremlingraph");
    }

    @Override
    public Map<Class<?>, String> getTypeDefinitions() {
        final ImmutableMap.Builder<Class<?>, String> builder = ImmutableMap.builder();

        builder.put(Map.class, "graph");

        return builder.build();
    }

    @Override
    public String getTypeNamespace() {
        return "gremlin";
    }
}
