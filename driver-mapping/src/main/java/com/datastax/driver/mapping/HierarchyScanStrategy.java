/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.mapping;

import java.util.List;

/**
 * A strategy to determine which ancestors of mapped classes should be scanned for mapped properties.
 */
public interface HierarchyScanStrategy {

    /**
     * Computes the ancestors of the given base class, optionally
     * filtering out any ancestor that should not be scanned.
     * <p/>
     * Implementors should always include {@code mappedClass}
     * in the returned list.
     *
     * @param mappedClass The mapped class; this is necessarily a class annotated with
     *                  either {@link com.datastax.driver.mapping.annotations.Table @Table} or
     *                  {@link com.datastax.driver.mapping.annotations.UDT @UDT}.
     * @return the list of classes that should be scanned,
     * including {@code mappedClass} itself and its ancestors,
     * ordered from the lowest (closest to {@code mappedClass})
     * to the highest (or farthest from {@code mappedClass}).
     */
    List<Class<?>> filterClassHierarchy(Class<?> mappedClass);

}
