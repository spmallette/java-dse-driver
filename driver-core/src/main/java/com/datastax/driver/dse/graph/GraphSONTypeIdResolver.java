/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides quick lookup for Type deserialization extracted from the JSON payload. As well as the Java Object to types
 * compatible for the version 2.0 of GraphSON.
 */
class GraphSONTypeIdResolver extends TypeIdResolverBase {

    private final Map<String, JavaType> idToType = new HashMap<String, JavaType>();

    private final Map<Class<?>, String> typeToId = new HashMap<Class<?>, String>();

    // Override manually a type definition.
    public GraphSONTypeIdResolver addCustomType(final String name, final Class<?> clasz, ObjectMapper objectMapper) {
        getIdToType().put(name, objectMapper.getTypeFactory().constructType(clasz));
        getTypeToId().put(clasz, name);
        return this;
    }

    @Override
    public String idFromValue(final Object o) {
        return idFromValueAndType(o, o.getClass());
    }

    @Override
    public String idFromValueAndType(final Object o, final Class<?> aClass) {
        if (!getTypeToId().containsKey(aClass)) {
            // If one wants to serialize an object with a type, but hasn't registered
            // a typeID for that class, fail.
            throw new IllegalArgumentException(String.format("Could not find a type identifier for the class : %s. " +
                    "Make sure the value to serialize has a type identifier registered for its class.", aClass));
        } else {
            return getTypeToId().get(aClass);
        }
    }

    @Override
    public JavaType typeFromId(final DatabindContext databindContext, final String s) {
        // Get the type from the string from the stored Map. If not found, default to deserialize as a String.
        return getIdToType().containsKey(s)
                ? getIdToType().get(s)
                // TODO: shouldn't we fail instead, if the type is not found? Or log something?
                : databindContext.constructType(String.class);
    }

    @Override
    public String getDescForKnownTypeIds() {
        return "GraphSONType";
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }

    private Map<String, JavaType> getIdToType() {
        return idToType;
    }

    private Map<Class<?>, String> getTypeToId() {
        return typeToId;
    }


}
