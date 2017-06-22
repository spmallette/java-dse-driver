/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.mapping.annotations;

import com.datastax.driver.core.Configuration;
import com.datastax.driver.core.QueryOptions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Query parameters to use in the (generated) implementation of a method of an {@link Accessor}
 * interface.
 * <p/>
 * All the parameters of this annotation are optional, and when not provided default to whatever
 * default the {@code Cluster} instance used underneath are (those set in
 * {@link Configuration#getQueryOptions}).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryParameters {
    /**
     * The consistency level to use for the operation.
     *
     * @return the consistency level to use for the operation.
     */
    String consistency() default "";

    /**
     * The fetch size to use for paging the result of this operation.
     *
     * @return the fetch size to use for the operation.
     */
    int fetchSize() default -1;

    /**
     * Whether tracing should be enabled for this operation.
     *
     * @return whether tracing should be enabled for this operation.
     */
    boolean tracing() default false;

    /**
     * Whether the operation is idempotent or not.
     * <p/>
     * When this attribute is set to {@code true} the operation is assumed idempotent;
     * when set to {@code false}, it is assumed that it is not.
     * <p/>
     * If a value of {@code true} or {@code false} is provided,
     * the underlying {@link com.datastax.driver.core.Statement}
     * will have its {@link com.datastax.driver.core.Statement#setIdempotent(boolean) idempotent flag}
     * set accordingly.
     * <p/>
     * When this attribute is set to its default
     * (an empty array), it means that the statement's idempotent flag
     * will not be set, and its idempotence will be inferred
     * from {@link QueryOptions#getDefaultIdempotence()}.
     * <p/>
     * This attribute is declared as a boolean array to allow
     * for "unset" values, but it can only contain at most
     * one element.
     *
     * @return {@code true} if the operation is idempotent, {@code false} otherwise.
     */
    boolean[] idempotent() default {};
}
