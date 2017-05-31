/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.mapping.annotations;

import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.mapping.MappingConfiguration;
import com.datastax.driver.mapping.NamingStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that allows to specify the name of the CQL column to which the
 * property should be mapped.
 * <p/>
 * Note that this annotation is generally optional in the sense that any field
 * or any getter method of a Java bean property of a class annotated by {@link Table}
 * will be mapped by default to a column
 * having the same name than this field / property, unless that field or method has the
 * {@link Transient} annotation. As such, this annotation is mainly useful when
 * the CQL column name does not correspond to the field or property name itself.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    /**
     * Name of the column being mapped in Cassandra. By default, the name returned
     * by the {@link NamingStrategy} of the current
     * {@link MappingConfiguration}.
     *
     * @return the name of the mapped column in Cassandra, or {@code ""} to use
     * the naming strategy.
     */
    String name() default "";

    /**
     * Whether the value return by {@link #name()} is case-sensitive (this has no
     * effect if no name is provided in the annotation).
     *
     * @return whether the column name is case-sensitive.
     */
    boolean caseSensitive() default false;

    /**
     * A custom codec that will be used to serialize and deserialize the column.
     *
     * @return the codec's class. It must have a no-argument constructor (the mapper
     * will create an instance and cache it).
     */
    Class<? extends TypeCodec<?>> codec() default Defaults.NoCodec.class;
}
