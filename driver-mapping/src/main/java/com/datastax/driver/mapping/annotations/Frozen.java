/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.mapping.annotations;

import com.datastax.driver.core.DataType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that the property decorated with this annotation maps to a CQL type that is {@link DataType#isFrozen() frozen},
 * or contains frozen subtypes.
 * <p/>
 * This annotation is purely informational at this stage, the validity of the declaration is not checked.
 * But will become useful when a schema generation feature is added to the mapper. Therefore it is a good idea to keep
 * frozen declarations up-to-date.
 *
 * @see FrozenKey
 * @see FrozenValue
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Frozen {

    // Implementation note: frozen annotations were previously checked at runtime, this code can be found in version 2.1.7

    /**
     * Contains the full CQL type of the target column. As a convenience, this can be left out when only the top-level
     * type is frozen.
     * <p/>
     * Examples:
     * <pre>
     * // Will map to frozen&lt;user&gt;
     * &#64;Frozen
     * private User user;
     *
     * &#64;Frozen("map&lt;text, map&lt;text, frozen&lt;user&gt;&gt;&gt;")
     * private Map&lt;String, Map&lt;String, User&gt;&gt; m;
     * </pre>
     * <p/>
     * Also consider the {@link FrozenKey @FrozenKey} and {@link FrozenValue @FrozenValue} shortcuts for simple collections.
     *
     * @return the full CQL type of the target column.
     */
    String value() default "";
}
