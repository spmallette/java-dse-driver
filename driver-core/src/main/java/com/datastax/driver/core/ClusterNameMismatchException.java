/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import java.net.InetSocketAddress;

/**
 * Indicates that we've attempted to connect to a node which cluster name doesn't match that of the other nodes known to the driver.
 */
class ClusterNameMismatchException extends Exception {

    private static final long serialVersionUID = 0;

    public final InetSocketAddress address;
    public final String expectedClusterName;
    public final String actualClusterName;

    public ClusterNameMismatchException(InetSocketAddress address, String actualClusterName, String expectedClusterName) {
        super(String.format("[%s] Host %s reports cluster name '%s' that doesn't match our cluster name '%s'. This host will be ignored.",
                address, address, actualClusterName, expectedClusterName));
        this.address = address;
        this.expectedClusterName = expectedClusterName;
        this.actualClusterName = actualClusterName;
    }
}
