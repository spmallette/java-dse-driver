/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

/**
 * Values for a SCHEMA_CHANGE event.
 * See protocol v4 section 4.2.6.
 * Note that {@code VIEW} is not a valid string under protocol v4 or lower, but is included for internal use only.
 */
enum SchemaElement {
    KEYSPACE, TABLE, TYPE, FUNCTION, AGGREGATE, VIEW
}
