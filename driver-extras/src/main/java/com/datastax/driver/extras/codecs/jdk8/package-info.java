/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
/**
 * This package contains a collection of convenience
 * {@link com.datastax.driver.core.TypeCodec TypeCodec} instances useful for
 * serializing between CQL types and Java 8 types, such as the ones from the
 * {@code java.time} API and {@code java.util.Optional}.
 * <p/>
 * <p/>
 * Note that, while the driver remains globally compatible with older JDKs,
 * classes in this package require the presence of a Java 8 or higher
 * at runtime.
 */
package com.datastax.driver.extras.codecs.jdk8;
