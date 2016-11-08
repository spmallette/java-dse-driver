/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

/**
 * Clustering orders.
 * <p/>
 * This is used by metadata classes to indicate the clustering
 * order of a clustering column in a table or materialized view.
 */
public enum ClusteringOrder {

    ASC, DESC;

}