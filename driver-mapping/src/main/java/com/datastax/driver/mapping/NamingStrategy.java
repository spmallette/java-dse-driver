/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.mapping;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Field;

/**
 * Determines how Java property names are translated to Cassandra column/field names for a mapped
 * class.
 * <p/>
 * This will be used for any property that doesn't have an explicit name provided (via a
 * {@link Column} or {@link Field} annotation).
 * <p/>
 * If you need to implement your own strategy, the most straightforward approach is to build a
 * {@link DefaultNamingStrategy#DefaultNamingStrategy(NamingConvention, NamingConvention)
 * DefaultNamingStrategy with explicit naming conventions}.
 */
public interface NamingStrategy {

    /**
     * Infers a Cassandra column/field name from a Java property name.
     *
     * @param javaPropertyName the name of the Java property. Depending on the
     *                         {@link DefaultPropertyMapper#setPropertyAccessStrategy(PropertyAccessStrategy)
     *                         property access strategy}, this might the name of the Java field, or
     *                         be inferred from a getter/setter based on the usual Java beans
     *                         conventions.
     * @return the name of the Cassandra column or field. If you want the mapping to be
     * case-insensitive, this should be in lower case.
     */
    String toCassandraName(String javaPropertyName);

}
