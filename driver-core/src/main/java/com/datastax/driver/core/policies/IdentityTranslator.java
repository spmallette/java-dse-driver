/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.policies;

import com.datastax.driver.core.Cluster;

import java.net.InetSocketAddress;

/**
 * The default {@link AddressTranslator} used by the driver that do no
 * translation.
 */
public class IdentityTranslator implements AddressTranslator {

    @Override
    public void init(Cluster cluster) {
        // Nothing to do
    }

    /**
     * Translates a Cassandra {@code rpc_address} to another address if necessary.
     * <p/>
     * This method is the identity function, it always return the address passed
     * in argument, doing no translation.
     *
     * @param address the address of a node as returned by Cassandra.
     * @return {@code address} unmodified.
     */
    @Override
    public InetSocketAddress translate(InetSocketAddress address) {
        return address;
    }

    @Override
    public void close() {
    }
}
