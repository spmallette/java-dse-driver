/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.mapping;

import java.util.Set;

/**
 * A pluggable component that maps
 * Java properties to a Cassandra objects.
 */
public interface PropertyMapper {

    /**
     * Maps the given table class.
     *
     * @param tableClass the table class.
     * @return a set of mapped properties for the given class.
     */
    Set<? extends MappedProperty<?>> mapTable(Class<?> tableClass);

    /**
     * Maps the given UDT class.
     *
     * @param udtClass the UDT class.
     * @return a set of mapped properties for the given class.
     */
    Set<? extends MappedProperty<?>> mapUdt(Class<?> udtClass);

}
