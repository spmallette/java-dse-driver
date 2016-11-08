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
 * Whenever this annotation is added on a property, the property will not be mapped
 * to any column (neither during reads nor writes).
 * <p/>
 * Please note that it is thus illegal to have a field that has both the
 * {@code Transient} annotation and one of the following annotations:
 * {@link Column}, {@link PartitionKey}, {@link ClusteringColumn}.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transient {
}
