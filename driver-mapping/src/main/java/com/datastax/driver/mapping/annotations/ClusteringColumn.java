/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.mapping.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for properties that map to a CQL clustering column.
 * <p/>
 * If the mapped table has multiple clustering columns, it is mandatory
 * to specify the ordinal parameter to avoid ordering ambiguity.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ClusteringColumn {
    /**
     * Ordinal to add when several clustering columns are declared within a single
     * entity.
     *
     * @return the ordinal value.
     */
    int value() default 0;
}
