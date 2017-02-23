/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.schemabuilder;

/**
 * The start of a statement, that another class will append to, to build the final statement.
 */
interface StatementStart {
    String buildInternal();
}
