/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.mapping;

import com.datastax.driver.mapping.annotations.UDT;
import com.google.common.collect.Sets;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility methods to determine which CQL type we expect for a given Java field type.
 */
class TypeMappings {

    private static boolean mapsToCollection(Class<?> klass) {
        return mapsToList(klass) || mapsToSet(klass) || mapsToMap(klass);
    }

    private static boolean mapsToList(Class<?> klass) {
        return List.class.equals(klass);
    }

    private static boolean mapsToSet(Class<?> klass) {
        return Set.class.equals(klass);
    }

    private static boolean mapsToMap(Class<?> klass) {
        return Map.class.equals(klass);
    }

    static boolean isMappedUDT(Class<?> klass) {
        return klass.isAnnotationPresent(UDT.class);
    }

    /**
     * Traverses the type of a Java field or parameter to find classes annotated with @UDT.
     * This will recurse into nested collections, e.g. List<Set<TheMappedUDT>>
     */
    static Set<Class<?>> findUDTs(Type type) {
        Set<Class<?>> udts = findUDTs(type, null);
        return (udts == null)
                ? Collections.<Class<?>>emptySet()
                : udts;
    }

    private static Set<Class<?>> findUDTs(Type type, Set<Class<?>> udts) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Type raw = pt.getRawType();
            if ((raw instanceof Class)) {
                Class<?> klass = (Class<?>) raw;
                if (mapsToCollection(klass)) {
                    Type[] childTypes = pt.getActualTypeArguments();
                    udts = findUDTs(childTypes[0], udts);

                    if (mapsToMap(klass))
                        udts = findUDTs(childTypes[1], udts);
                }
            }
        } else if (type instanceof Class) {
            Class<?> klass = (Class<?>) type;
            if (isMappedUDT(klass)) {
                if (udts == null)
                    udts = Sets.newHashSet();
                udts.add(klass);
            }
        }
        return udts;
    }
}
