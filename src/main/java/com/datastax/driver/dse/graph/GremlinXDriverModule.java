/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Map;

class GremlinXDriverModule extends GraphSON2JacksonModule {

    public GremlinXDriverModule() {
        super("graph-graphson2extended");
    }

    @Override
    public Map<Class<?>, String> getTypeDefinitions() {
        // Override the TinkerPop classes' types.
        final ImmutableMap.Builder<Class<?>, String> builder = ImmutableMap.builder();

        builder.put(InetAddress.class, "InetAddress");
        builder.put(ByteBuffer.class, "ByteBuffer");
        builder.put(Short.class, "Int16");
        builder.put(BigInteger.class, "BigInteger");
        builder.put(BigDecimal.class, "BigDecimal");
        builder.put(Byte.class, "Byte");
        builder.put(Character.class, "Char");

        return builder.build();
    }

    @Override
    public String getTypeNamespace() {
        return "gx";
    }
}
