/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;


import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

import java.util.Collection;

/**
 * Creates the Type serializers as well as the Typed deserializers that will be provided to the serializers and
 * deserializers. Contains the typeInfo level that should be provided by the GraphSONMapper.
 */
class GraphSONTypeResolverBuilder extends StdTypeResolverBuilder {

    private TypeInfo typeInfo;
    private String valuePropertyName;

    @Override
    public TypeDeserializer buildTypeDeserializer(final DeserializationConfig config, final JavaType baseType,
                                                  final Collection<NamedType> subtypes) {
        final TypeIdResolver idRes = this.idResolver(config, baseType, subtypes, false, true);
        return new GraphSONTypeDeserializer(baseType, idRes, this.getTypeProperty(), typeInfo, valuePropertyName);
    }


    @Override
    public TypeSerializer buildTypeSerializer(final SerializationConfig config, final JavaType baseType,
                                              final Collection<NamedType> subtypes) {
        final TypeIdResolver idRes = this.idResolver(config, baseType, subtypes, true, false);
        return new GraphSONTypeSerializer(idRes, this.getTypeProperty(), typeInfo, valuePropertyName);
    }

    public GraphSONTypeResolverBuilder valuePropertyName(final String valuePropertyName) {
        this.valuePropertyName = valuePropertyName;
        return this;
    }

    public GraphSONTypeResolverBuilder typesEmbedding(final TypeInfo typeInfo) {
        this.typeInfo = typeInfo;
        return this;
    }
}
