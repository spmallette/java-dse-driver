/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

/**
 * Defines how data types are handled in GraphSON through the {@link GraphSON2Mapper}.
 */
enum TypeInfo {
    NO_TYPES,
    PARTIAL_TYPES
}
