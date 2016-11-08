/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.schemabuilder;

/**
 * Wrapper around UDT and non-UDT types.
 * <p/>
 * The reason for this interface is that the core API doesn't let us build {@link com.datastax.driver.core.DataType}s representing UDTs, we have to obtain
 * them from the cluster metadata. Since we want to use SchemaBuilder without a Cluster instance, UDT types will be provided via
 * {@link UDTType} instances.
 */
interface ColumnType {
    String asCQLString();
}
