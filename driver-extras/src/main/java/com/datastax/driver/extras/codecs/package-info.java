/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
/**
 * This package and its subpackages contain several convenience {@link com.datastax.driver.core.TypeCodec TypeCodec}s.
 * <p/>
 * <table summary="Supported Mappings">
 * <tr>
 * <th>Package</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>{@link com.datastax.driver.extras.codecs.arrays}</td>
 * <td>codecs mapping CQL lists to Java arrays.</td>
 * </tr>
 * <tr>
 * <td>{@link com.datastax.driver.extras.codecs.date}</td>
 * <td>codecs mapping CQL temporal types to Java primitive types.</td>
 * </tr>
 * <tr>
 * <td>{@link com.datastax.driver.extras.codecs.enums}</td>
 * <td>codecs mapping CQL types to Java enums.</td>
 * </tr>
 * <tr>
 * <td>{@link com.datastax.driver.extras.codecs.guava}</td>
 * <td>codecs mapping CQL types to Guava-specific Java types.</td>
 * </tr>
 * <tr>
 * <td>{@link com.datastax.driver.extras.codecs.jdk8}</td>
 * <td>codecs mapping CQL types to Java 8 types, including {@code java.time} API and {@code java.util.Optional}.</td>
 * </tr>
 * <tr>
 * <td>{@link com.datastax.driver.extras.codecs.joda}</td>
 * <td>codecs mapping CQL types to Joda Time types.</td>
 * </tr>
 * <tr>
 * <td>{@link com.datastax.driver.extras.codecs.json}</td>
 * <td>codecs mapping CQL varchars to JSON structures.</td>
 * </tr>
 * </table>
 */
package com.datastax.driver.extras.codecs;
