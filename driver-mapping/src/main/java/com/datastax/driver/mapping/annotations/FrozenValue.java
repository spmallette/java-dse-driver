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
 * Shorthand to specify that the value type of a collection property is frozen.
 * <p/>
 * This is equivalent to any of the following:
 * <ul>
 * <li>{@code @Frozen("list<frozen<foo>>")}</li>
 * <li>{@code @Frozen("set<frozen<foo>>")}</li>
 * <li>{@code @Frozen("map<foo, frozen<bar>>")}</li>
 * </ul>
 *
 * @see Frozen
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FrozenValue {
}
