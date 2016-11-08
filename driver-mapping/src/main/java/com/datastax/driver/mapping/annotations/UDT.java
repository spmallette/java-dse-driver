/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.mapping.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines to which User Defined Type a class must be mapped to.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UDT {
    /**
     * The name of the keyspace the type is part of.
     *
     * @return the name of the keyspace.
     */
    String keyspace() default "";

    /**
     * The name of the type.
     *
     * @return the name of the type.
     */
    String name();

    /**
     * Whether the keyspace name is a case sensitive one.
     *
     * @return whether the keyspace name is a case sensitive one.
     */
    boolean caseSensitiveKeyspace() default false;

    /**
     * Whether the type name is a case sensitive one.
     *
     * @return whether the type name is a case sensitive one.
     */
    boolean caseSensitiveType() default false;
}
