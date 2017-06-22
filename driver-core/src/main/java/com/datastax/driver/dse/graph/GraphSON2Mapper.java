/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;

import java.sql.Timestamp;
import java.util.*;

/**
 * An extension to the standard Jackson {@code ObjectMapper} which automatically registers the standard
 * {@link GraphSON2GremlinDriverModule} for serializing Graph elements.  This class
 * can be used for generalized JSON serialization tasks that require meeting GraphSON standards.
 */
class GraphSON2Mapper {

    private final List<SimpleModule> customModules;
    private final boolean loadCustomSerializers;
    private final boolean normalize;
    private final TypeInfo typeInfo;

    private GraphSON2Mapper(final Builder builder) {
        this.customModules = builder.customModules;
        this.loadCustomSerializers = builder.loadCustomModules;
        this.normalize = builder.normalize;
        this.typeInfo = builder.typeInfo;
    }

    ObjectMapper createMapper() {
        final ObjectMapper om = new ObjectMapper();
        om.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        for (SimpleModule module : customModules) {
            om.registerModule(module);
        }

        // plugin external serialization modules
        if (loadCustomSerializers)
            om.findAndRegisterModules();


        final GraphSONTypeIdResolver graphSONTypeIdResolver = new GraphSONTypeIdResolver();
        final TypeResolverBuilder<?> typer = new GraphSONTypeResolverBuilder()
                .typesEmbedding(getTypeInfo())
                .valuePropertyName("@value")
                .init(JsonTypeInfo.Id.CUSTOM, graphSONTypeIdResolver)
                .typeProperty("@type");

        // Registers native Java types that are supported by Jackson
        registerJavaBaseTypes(graphSONTypeIdResolver, om);

        // Register types to typeResolver for the Custom modules
        for (SimpleModule module : customModules) {
            if (module instanceof GraphSON2JacksonModule) {
                final GraphSON2JacksonModule mod = (GraphSON2JacksonModule) module;
                final Map<Class<?>, String> moduleTypeDefinitions = mod.getTypeDefinitions();
                if (moduleTypeDefinitions != null) {
                    if (mod.getTypeNamespace() == null || mod.getTypeNamespace().isEmpty())
                        throw new IllegalStateException("Cannot specify a module for GraphSON 2.0 with type definitions but without a type Domain. " +
                                "If no specific type domain is required, use Gremlin's default domain, \"g\" but there may be collisions.");

                    for (Map.Entry<Class<?>, String> typeDef : moduleTypeDefinitions.entrySet()) {
                        graphSONTypeIdResolver.addCustomType(String.format("%s:%s", mod.getTypeNamespace(), typeDef.getValue()), typeDef.getKey(), om);
                    }
                }
            }
        }
        om.setDefaultTyping(typer);

        // this provider toStrings all unknown classes and converts keys in Map objects that are Object to String.
        final DefaultSerializerProvider provider = new GraphSONSerializerProvider();
        om.setSerializerProvider(provider);

        if (normalize)
            om.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

        // keep streams open to accept multiple values (e.g. multiple vertices)
        om.getFactory().disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        return om;
    }

    static Builder build() {
        return new Builder();
    }

    TypeInfo getTypeInfo() {
        return this.typeInfo;
    }
    
    private void registerJavaBaseTypes(final GraphSONTypeIdResolver graphSONTypeIdResolver, ObjectMapper om) {
        graphSONTypeIdResolver.addCustomType(String.format("%s:%s", "g", UUID.class.getSimpleName()), UUID.class, om);
        graphSONTypeIdResolver.addCustomType(String.format("%s:%s", "g", Class.class.getSimpleName()), Class.class, om);
        graphSONTypeIdResolver.addCustomType(String.format("%s:%s", "g", Calendar.class.getSimpleName()), Calendar.class, om);
        graphSONTypeIdResolver.addCustomType(String.format("%s:%s", "g", Date.class.getSimpleName()), Date.class, om);
        graphSONTypeIdResolver.addCustomType(String.format("%s:%s", "g", TimeZone.class.getSimpleName()), TimeZone.class, om);
        graphSONTypeIdResolver.addCustomType(String.format("%s:%s", "g", Timestamp.class.getSimpleName()), Timestamp.class, om);
    }

    static class Builder {
        private final List<SimpleModule> customModules = new ArrayList<SimpleModule>();
        private final boolean loadCustomModules = false;
        private final boolean normalize = false;
        // GraphSON 2.0 should have types activated by default, otherwise use there's no point in using it instead of 1.0.
        private final TypeInfo typeInfo = TypeInfo.PARTIAL_TYPES;

        private Builder() {
        }

        /**
         * Supply a mapper module for serialization/deserialization.
         */
        Builder addCustomModule(final SimpleModule custom) {
            this.customModules.add(custom);
            return this;
        }

        GraphSON2Mapper create() {
            return new GraphSON2Mapper(this);
        }
    }
}
