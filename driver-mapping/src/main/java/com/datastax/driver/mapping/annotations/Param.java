/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.mapping.annotations;

import com.datastax.driver.core.TypeCodec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides a name for a parameter of a method in an {@link Accessor} interface that
 * can be used to reference to that parameter in method {@link Query}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
    /**
     * The name for the parameter
     *
     * @return the name of the parameter.
     */
    String value() default "";

    /**
     * A custom codec that will be used to serialize the parameter.
     *
     * @return the codec's class. It must have a no-argument constructor (the mapper
     * will create an instance and cache it).
     */
    Class<? extends TypeCodec<?>> codec() default Defaults.NoCodec.class;
}
