/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
/**
 * This package contains a collection of convenience
 * {@link com.datastax.driver.core.TypeCodec TypeCodec} instances useful for
 * serializing between CQL types and Joda Time types such as {@link org.joda.time.DateTime}.
 * <p/>
 * <p/>
 * Note that classes in this package require the presence of
 * <a href="http://www.joda.org/joda-time/">Joda Time library</a> at runtime.
 * If you use Maven, this can be done by declaring the following dependency in your project:
 * <p/>
 * <pre>{@code
 * <dependency>
 *   <groupId>joda-time</groupId>
 *   <artifactId>joda-time</artifactId>
 *   <version>2.9.1</version>
 * </dependency>
 * }</pre>
 */
package com.datastax.driver.extras.codecs.joda;
