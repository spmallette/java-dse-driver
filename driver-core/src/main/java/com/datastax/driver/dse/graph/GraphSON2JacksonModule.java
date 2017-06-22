/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.Map;

/**
 * A {@link SimpleModule} extension that does the necessary work to make the automatic typed deserialization
 * without full canonical class names work.
 *
 * Users of custom modules with the GraphSONMapper that want their objects to be deserialized automatically by the
 * mapper, must extend this class with their module. It is the only required step.
 *
 * Using this basis module allows the serialization and deserialization of typed objects without having the whole
 * canonical name of the serialized classes included in the Json payload. This is also necessary because Java
 * does not provide an agnostic way to search in a classpath a find a class by its simple name. Although that could
 * be done with an external library, later if we deem it necessary.
 */
abstract class GraphSON2JacksonModule extends SimpleModule {

    public GraphSON2JacksonModule(final String name) {
        super(name);
    }

    public abstract Map<Class<?>, String> getTypeDefinitions();

    public abstract String getTypeNamespace();
}
