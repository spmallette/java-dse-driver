/*
 *      Copyright (C) 2012-2017 DataStax Inc.
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
 * Annotation for properties that map to a CQL partition key (or one of it's
 * component if the partition key is composite).
 * <p/>
 * If the partition key of the mapped table is composite, it is mandatory
 * to specify the ordinal parameter to avoid ordering ambiguity.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PartitionKey {
    /**
     * Ordinal to add when the partition key has multiple components.
     *
     * @return the ordinal to use.
     */
    int value() default 0;
}
